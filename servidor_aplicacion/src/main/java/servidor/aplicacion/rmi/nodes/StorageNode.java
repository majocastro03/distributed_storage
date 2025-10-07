package servidor.aplicacion.rmi.nodes;

import servidor.aplicacion.interfaces.NodeInterface;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class StorageNode extends UnicastRemoteObject implements NodeInterface {
    private static final Logger logger = Logger.getLogger(StorageNode.class.getName());

    private final String nodeName;
    private final String storagePath;
    private String status;
    private final ConcurrentHashMap<String, String> fileIndex;
    private long availableSpace;
    private long usedSpace;

    public StorageNode(String nodeName, String storagePath) throws RemoteException {
        super();
        this.nodeName = nodeName;
        this.storagePath = storagePath;
        this.status = "ACTIVE";
        this.fileIndex = new ConcurrentHashMap<>();
        this.availableSpace = 0L;
        this.usedSpace = 0L;

        // Crear directorio de almacenamiento para este nodo
        createStorageDirectory();

        // Calcular espacio disponible en disco para el nodo
        calculateDiskSpace();

        // Cargar archivos existentes en el índice del nodo
        loadFileIndex();

        logger.info("Storage node initialized: " + nodeName + " at " + storagePath);
    }

    @Override
    public boolean storeFileChunk(String fileId, int chunkNumber, byte[] data) throws RemoteException {
        try {
            String fileName = generateChunkFileName(fileId, chunkNumber);
            Path filePath = Paths.get(storagePath, fileName);

            // Verificar si hay espacio suficiente en el nodo
            if (availableSpace < data.length) {
                logger.warning("Insufficient space to store chunk. Available: " + availableSpace + ", Required: "
                        + data.length);
                return false;
            }

            // Escribir chunk al almacenamiento del nodo
            Files.write(filePath, data);

            // Actualizar índice de archivos del nodo
            String key = fileId + "_" + chunkNumber;
            fileIndex.put(key, filePath.toString());

            // Actualizar estadísticas de uso de espacio del nodo
            usedSpace += data.length;
            availableSpace -= data.length;

            logger.info("Stored chunk " + chunkNumber + " of file " + fileId + " (" + data.length + " bytes)");
            return true;

        } catch (IOException e) {
            logger.severe("Error storing file chunk: " + e.getMessage());
            return false;
        }
    }

    @Override
    public byte[] retrieveFileChunk(String fileId, int chunkNumber) throws RemoteException {
        try {
            String key = fileId + "_" + chunkNumber;
            String filePath = fileIndex.get(key);

            if (filePath == null) {
                logger.warning("Chunk not found: " + key);
                return null;
            }

            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                logger.warning("File not found on disk: " + filePath);
                fileIndex.remove(key); // Limpiar índice de archivos del nodo
                return null;
            }

            byte[] data = Files.readAllBytes(path);
            logger.info("Retrieved chunk " + chunkNumber + " of file " + fileId + " (" + data.length + " bytes)");
            return data;

        } catch (IOException e) {
            logger.severe("Error retrieving file chunk: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean deleteFileChunk(String fileId, int chunkNumber) throws RemoteException {
        try {
            String key = fileId + "_" + chunkNumber;
            String filePath = fileIndex.get(key);

            if (filePath == null) {
                logger.warning("Chunk not found for deletion: " + key);
                return false;
            }

            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                long fileSize = Files.size(path);
                Files.delete(path);

                // Actualizar estadísticas de espacio del nodo
                usedSpace -= fileSize;
                availableSpace += fileSize;
            }

            // Remover del índice
            fileIndex.remove(key);

            logger.info("Deleted chunk " + chunkNumber + " of file " + fileId);
            return true;

        } catch (IOException e) {
            logger.severe("Error deleting file chunk: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> listStoredFiles() throws RemoteException {
        List<String> fileIds = new ArrayList<>();

        for (String key : fileIndex.keySet()) {
            String fileId = key.split("_")[0]; // Extraer ID de archivo del identificador del chunk
            if (!fileIds.contains(fileId)) {
                fileIds.add(fileId);
            }
        }

        return fileIds;
    }

    @Override
    public String getNodeStatus() throws RemoteException {
        return status;
    }

    @Override
    public long getAvailableSpace() throws RemoteException {
        calculateDiskSpace(); // Actualizar espacio en tiempo real
        return availableSpace;
    }

    @Override
    public long getUsedSpace() throws RemoteException {
        return usedSpace;
    }

    @Override
    public boolean ping() throws RemoteException {
        return "ACTIVE".equals(status);
    }

    @Override
    public void updateNodeInfo(String status, long availableSpace, long usedSpace) throws RemoteException {
        this.status = status;
        this.availableSpace = availableSpace;
        this.usedSpace = usedSpace;
        logger.info(
                "Node info updated - Status: " + status + ", Available: " + availableSpace + ", Used: " + usedSpace);
    }

    @Override
    public boolean isHealthy() throws RemoteException {
        try {
            // Verificar acceso al directorio de almacenamiento del nodo
            Path storageDirPath = Paths.get(storagePath);
            if (!Files.exists(storageDirPath) || !Files.isWritable(storageDirPath)) {
                return false;
            }

            // Verificar estado operativo del nodo
            if (!"ACTIVE".equals(status)) {
                return false;
            }

            // Verificar espacio mínimo requerido en el nodo (100MB)
            return availableSpace > 100 * 1024 * 1024;

        } catch (Exception e) {
            logger.warning("Health check failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getFileList() throws RemoteException {
        return new ArrayList<>(fileIndex.keySet());
    }

    @Override
    public boolean hasFile(String fileId, int chunkNumber) throws RemoteException {
        String key = fileId + "_" + chunkNumber;
        return fileIndex.containsKey(key);
    }

    @Override
    public boolean replicateChunk(String fileId, int chunkNumber, byte[] data, String targetNodeUrl)
            throws RemoteException {
        try {
            // Conectar con nodo destino para replicación
            NodeInterface targetNode = (NodeInterface) Naming.lookup(targetNodeUrl);

            // Enviar chunk al nodo destino
            boolean success = targetNode.storeFileChunk(fileId, chunkNumber, data);

            if (success) {
                logger.info(
                        "Successfully replicated chunk " + chunkNumber + " of file " + fileId + " to " + targetNodeUrl);
            } else {
                logger.warning(
                        "Failed to replicate chunk " + chunkNumber + " of file " + fileId + " to " + targetNodeUrl);
            }

            return success;

        } catch (Exception e) {
            logger.severe("Error replicating chunk: " + e.getMessage());
            return false;
        }
    }

    // Métodos auxiliares privados
    private void createStorageDirectory() {
        try {
            Path storageDirPath = Paths.get(storagePath);
            if (!Files.exists(storageDirPath)) {
                Files.createDirectories(storageDirPath);
                logger.info("Created storage directory: " + storagePath);
            }
        } catch (IOException e) {
            logger.severe("Failed to create storage directory: " + e.getMessage());
            throw new RuntimeException("Cannot create storage directory", e);
        }
    }

    private void calculateDiskSpace() {
        try {
            Path storageDirPath = Paths.get(storagePath);
            long totalSpace = storageDirPath.toFile().getTotalSpace();
            long freeSpace = storageDirPath.toFile().getFreeSpace();

            this.availableSpace = freeSpace;
            this.usedSpace = totalSpace - freeSpace;

        } catch (Exception e) {
            logger.warning("Failed to calculate disk space: " + e.getMessage());
        }
    }

    private void loadFileIndex() {
        try {
            Path storageDirPath = Paths.get(storagePath);
            if (!Files.exists(storageDirPath)) {
                return;
            }

            Files.list(storageDirPath)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        String fileName = filePath.getFileName().toString();
                        if (fileName.matches(".*_chunk_\\d+\\.dat")) {
                            // Extraer fileId y chunkNumber del nombre del archivo
                            String[] parts = fileName.replace(".dat", "").split("_chunk_");
                            if (parts.length == 2) {
                                String fileId = parts[0];
                                String chunkNumber = parts[1];
                                String key = fileId + "_" + chunkNumber;
                                fileIndex.put(key, filePath.toString());
                            }
                        }
                    });

            logger.info("Loaded file index with " + fileIndex.size() + " entries");

        } catch (IOException e) {
            logger.warning("Failed to load file index: " + e.getMessage());
        }
    }

    private String generateChunkFileName(String fileId, int chunkNumber) {
        return fileId + "_chunk_" + chunkNumber + ".dat";
    }

    // Método para obtener el nombre del nodo
    public String getNodeName() {
        return nodeName;
    }

    // Método para cambiar el estado del nodo
    public void setStatus(String status) {
        this.status = status;
        logger.info("Node status changed to: " + status);
    }
}