package servidor.aplicacion.service;

import servidor.aplicacion.dao.FileDAO;
import servidor.aplicacion.dao.FileChunkDAO;
import servidor.aplicacion.manager.NodeManager;
import servidor.aplicacion.model.File;
import servidor.aplicacion.model.FileChunk;
import servidor.aplicacion.model.Node;
import servidor.aplicacion.rmi.RMIStorageNodeClient;
import servidor.aplicacion.interfaces.FileInterface;

import java.util.*;
import java.util.logging.Logger;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servicio de archivos distribuido que implementa:
 * - Fragmentación de archivos en chunks
 * - Distribución a múltiples nodos
 * - Replicación para redundancia
 * - Recuperación automática de fallos
 */
public class DistributedFileService implements FileInterface {

    private static final Logger logger = Logger.getLogger(DistributedFileService.class.getName());

    // Configuración de distribución
    private static final int CHUNK_SIZE = 1024 * 1024;
    private static final int REPLICATION_FACTOR = 2;

    private final FileDAO fileDAO;
    private final FileChunkDAO fileChunkDAO;
    private final NodeManager nodeManager;
    private final ExecutorService executorService;

    public DistributedFileService(FileDAO fileDAO, FileChunkDAO fileChunkDAO, NodeManager nodeManager) {
        this.fileDAO = fileDAO;
        this.fileChunkDAO = fileChunkDAO;
        this.nodeManager = nodeManager;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public File uploadFile(File file, byte[] data, long userId) throws Exception {
    logger.info("Iniciando upload distribuido para archivo: " + file.getName());
    logger.info("Tamaño de datos recibidos: " + (data != null ? data.length : "null"));

        // Verificar permisos de usuario
        if (file.getOwnerId() != userId) {
            throw new SecurityException("Usuario no es propietario del archivo");
        }

        // 2. Guardar metadatos del archivo en BD
        file.setSize((long) data.length);
        File savedFile = fileDAO.save(file);

        // 3. Fragmentar archivo en chunks
        List<byte[]> chunks = fragmentFile(data);
        logger.info("Archivo fragmentado en " + chunks.size() + " chunks");

        // 4. Distribuir chunks a nodos
        distributeChunks(savedFile.getId(), chunks);

        logger.info("Upload distribuido completado para archivo: " + file.getName());
        return savedFile;
    }

    @Override
    public byte[] downloadFile(long fileId, long userId) throws Exception {
        logger.info("Iniciando download distribuido para archivo ID: " + fileId);

        // 1. Obtener metadatos del archivo
        File file = fileDAO.findById(fileId);
        if (file == null) {
            throw new Exception("Archivo no encontrado");
        }

        // Verificar permisos de acceso
        if (file.getOwnerId() != userId) {
            throw new SecurityException("Usuario no tiene acceso al archivo");
        }

        if (!file.isFile()) {
            throw new Exception("No se puede descargar un directorio");
        }

        // 3. Obtener información de chunks
        List<FileChunk> chunks = fileChunkDAO.findByFileId(fileId);
        if (chunks.isEmpty()) {
            logger.warning("No se encontraron chunks para el archivo: " + fileId);
            return new byte[0];
        }

        // 4. Recuperar chunks de nodos
        byte[] reconstructedFile = reconstructFile(chunks);

        logger.info("Download distribuido completado para archivo ID: " + fileId);
        return reconstructedFile;
    }

    @Override
    public List<File> listFiles(long parentId, long userId) {
        return fileDAO.findByParentIdAndUser(parentId, userId);
    }

    @Override
    public void deleteFile(long fileId, long userId) throws Exception {
        logger.info("Iniciando eliminación distribuida para archivo ID: " + fileId);

        // Verificar archivo y permisos de usuario
        File file = fileDAO.findById(fileId);
        if (file == null) {
            throw new Exception("Archivo no encontrado");
        }

        if (file.getOwnerId() != userId) {
            throw new SecurityException("Usuario no tiene acceso al archivo");
        }

        // 2. Eliminar chunks de nodos
        List<FileChunk> chunks = fileChunkDAO.findByFileId(fileId);
        deleteChunksFromNodes(chunks);

        // 3. Eliminar chunks de BD
        fileChunkDAO.deleteByFileId(fileId);

        // 4. Eliminar archivo de BD
        fileDAO.delete(fileId);

        logger.info("Eliminación distribuida completada para archivo ID: " + fileId);
    }

    public File createDirectory(String name, Long parentId, long userId) throws Exception {
        File directory = new File(name, parentId, userId, File.DIR_TYPE, 0L);
        return fileDAO.save(directory);
    }

    public List<File> getUserFiles(long userId) {
        return fileDAO.findByOwnerId(userId);
    }

    public File getFileInfo(long fileId, long userId) throws Exception {
        File file = fileDAO.findById(fileId);

        if (file == null) {
            throw new Exception("Archivo no encontrado");
        }

        if (file.getOwnerId() != userId) {
            throw new SecurityException("Usuario no tiene acceso al archivo");
        }

        return file;
    }

    /**
     * Fragmenta un archivo en chunks de tamaño fijo
     */
    private List<byte[]> fragmentFile(byte[] data) {
        List<byte[]> chunks = new ArrayList<>();

        for (int i = 0; i < data.length; i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, data.length);
            byte[] chunk = Arrays.copyOfRange(data, i, end);
            chunks.add(chunk);
        }

        return chunks;
    }

    /**
     * Distribuye chunks a nodos disponibles con replicación
     */
    private void distributeChunks(Long fileId, List<byte[]> chunks) throws Exception {
        logger.info("Iniciando distribución de chunks para archivo ID: " + fileId);
        logger.info("Cantidad de chunks a distribuir: " + chunks.size());
        List<Node> availableNodes = nodeManager.getOnlineNodes();
        logger.info("Nodos disponibles: " + availableNodes.size());
        if (availableNodes.size() < REPLICATION_FACTOR) {
            logger.severe("No hay suficientes nodos disponibles para replicación. Requeridos: " + REPLICATION_FACTOR);
            throw new Exception("No hay suficientes nodos disponibles para replicación");
        }
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            final int chunkIndex = i;
            final byte[] chunkData = chunks.get(i);
            final String chunkHash = calculateHash(chunkData);
            logger.info("Preparando chunk " + chunkIndex + " (hash: " + chunkHash + ", tamaño: " + chunkData.length + ")");
            List<Node> selectedNodes = selectNodesForChunk(availableNodes, chunkIndex);
            logger.info("Chunk " + chunkIndex + " será distribuido a " + selectedNodes.size() + " nodos");
            for (Node node : selectedNodes) {
                logger.info("Enviando chunk " + chunkIndex + " al nodo ID: " + node.getId() + " IP: " + node.getIp() + ":" + node.getPort());
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        storeChunkInNode(fileId, chunkIndex, chunkData, chunkHash, node);
                    } catch (Exception e) {
                        logger.severe("Error almacenando chunk " + chunkIndex + " en nodo " + node.getIp() + ":" + node.getPort() + " - " + e.getMessage());
                        nodeManager.markNodeAsOffline(node.getId());
                    }
                }, executorService);
                futures.add(future);
            }
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        logger.info("Distribución de chunks finalizada para archivo ID: " + fileId);
    }

    /**
     * Almacena un chunk en un nodo específico y registra en BD
     */
    private void storeChunkInNode(Long fileId, int chunkIndex, byte[] chunkData,
            String chunkHash, Node node) throws Exception {

        logger.info("Intentando conectar al nodo ID: " + node.getId() + " RMI: " + node.getRmiUrl());
        RMIStorageNodeClient client = new RMIStorageNodeClient(node.getRmiUrl());
        if (!client.connect()) {
            logger.severe("No se pudo conectar al nodo: " + node.getRmiUrl());
            throw new Exception("No se pudo conectar al nodo: " + node.getRmiUrl());
        }
        try {
            String chunkId = fileId + "_" + chunkIndex;
            logger.info("Almacenando chunk en nodo. chunkId: " + chunkId + ", chunkIndex: " + chunkIndex);
            boolean stored = client.storeFileChunk(chunkId, chunkIndex, chunkData);
            if (!stored) {
                logger.severe("Nodo rechazó el almacenamiento del chunk " + chunkIndex);
                throw new Exception("Nodo rechazó el almacenamiento del chunk");
            }
            FileChunk chunk = new FileChunk();
            chunk.setFileId(fileId);
            chunk.setChunkIndex(chunkIndex);
            chunk.setNodeId(node.getId());
            chunk.setChecksum(chunkHash);
            chunk.setReplicated(false);
            logger.info("Registrando chunk en BD: file_id=" + fileId + ", node_id=" + node.getId() + ", chunk_index=" + chunkIndex);
            fileChunkDAO.save(chunk);
            logger.info("Chunk " + chunkIndex + " almacenado exitosamente en nodo " + node.getIp() + ":" + node.getPort());
        } finally {
            client.disconnect();
        }
    }

    /**
     * Reconstruye un archivo a partir de sus chunks
     */
    private byte[] reconstructFile(List<FileChunk> chunks) throws Exception {
        // Ordenar chunks por número
        chunks.sort(Comparator.comparingInt(FileChunk::getChunkIndex));

        List<byte[]> chunkData = new ArrayList<>();
        int totalSize = 0;

        // Lista para tareas asíncronas de recuperación
        List<CompletableFuture<byte[]>> futures = new ArrayList<>();

        for (FileChunk chunk : chunks) {
            CompletableFuture<byte[]> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return retrieveChunkFromNodes(chunk);
                } catch (Exception e) {
                    logger.severe("Error recuperando chunk " + chunk.getChunkIndex() +
                            ": " + e.getMessage());
                    return null;
                }
            }, executorService);

            futures.add(future);
        }

        // Esperar y recolectar resultados
        for (int i = 0; i < futures.size(); i++) {
            byte[] data = futures.get(i).join();
            if (data == null) {
                throw new Exception("No se pudo recuperar chunk " + chunks.get(i).getChunkIndex());
            }
            chunkData.add(data);
            totalSize += data.length;
        }

        // Reconstruir archivo
        byte[] result = new byte[totalSize];
        int offset = 0;

        for (byte[] data : chunkData) {
            System.arraycopy(data, 0, result, offset, data.length);
            offset += data.length;
        }

        return result;
    }

    // Recupera un chunk de los nodos, probando réplicas si es necesario

    private byte[] retrieveChunkFromNodes(FileChunk chunk) throws Exception {
        // Obtener todas las réplicas de este chunk
        List<FileChunk> replicas = fileChunkDAO.findByFileIdAndChunkNumber(
                chunk.getFileId(), chunk.getChunkIndex());

        for (FileChunk replica : replicas) {
            try {
                Node node = nodeManager.getNodeById(replica.getNodeId());
                if (node == null || !node.isOnline()) {
                    continue;
                }

                RMIStorageNodeClient client = new RMIStorageNodeClient(node.getRmiUrl());
                if (!client.connect()) {
                    continue;
                }

                try {
                    String chunkId = chunk.getFileId() + "_" + chunk.getChunkIndex();
                    byte[] data = client.retrieveFileChunk(chunkId, chunk.getChunkIndex());

                    if (data != null && verifyChunkIntegrity(data, replica.getChecksum())) {
                        return data;
                    }
                } finally {
                    client.disconnect();
                }

            } catch (Exception e) {
                logger.warning("Error recuperando chunk de nodo: " + e.getMessage());
                // Continuar con la siguiente réplica
            }
        }

        throw new Exception("No se pudo recuperar chunk " + chunk.getChunkIndex() +
                " de ningún nodo disponible");
    }

    // Elimina chunks de todos los nodos

    private void deleteChunksFromNodes(List<FileChunk> chunks) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (FileChunk chunk : chunks) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Node node = nodeManager.getNodeById(chunk.getNodeId());
                    if (node != null && node.isOnline()) {
                        RMIStorageNodeClient client = new RMIStorageNodeClient(node.getRmiUrl());
                        if (client.connect()) {
                            try {
                                String chunkId = chunk.getFileId() + "_" + chunk.getChunkIndex();
                                client.deleteFileChunk(chunkId, chunk.getChunkIndex());
                            } finally {
                                client.disconnect();
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Error eliminando chunk: " + e.getMessage());
                }
            }, executorService);

            futures.add(future);
        }

        // Esperar a que terminen todas las eliminaciones
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    // Selecciona nodos para almacenar un chunk (con replicación)

    private List<Node> selectNodesForChunk(List<Node> availableNodes, int chunkIndex) {
        List<Node> selectedNodes = new ArrayList<>();

        // Algoritmo simple de selección round-robin con offset
        for (int i = 0; i < REPLICATION_FACTOR && i < availableNodes.size(); i++) {
            int nodeIndex = (chunkIndex + i) % availableNodes.size();
            selectedNodes.add(availableNodes.get(nodeIndex));
        }

        return selectedNodes;
    }

    // Calcula el hash de un array de bytes
    private String calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            logger.severe("Error calculando hash: " + e.getMessage());
            return "";
        }
    }
    // Verifica la integridad de un chunk

    private boolean verifyChunkIntegrity(byte[] data, String expectedHash) {
        String actualHash = calculateHash(data);
        return actualHash.equals(expectedHash);
    }

    // Cierra el servicio liberando recursos

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}