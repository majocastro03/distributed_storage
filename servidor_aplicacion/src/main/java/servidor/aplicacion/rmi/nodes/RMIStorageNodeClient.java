package servidor.aplicacion.rmi.nodes;

import servidor.aplicacion.interfaces.NodeInterface;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

public class RMIStorageNodeClient {
    private static final Logger logger = Logger.getLogger(RMIStorageNodeClient.class.getName());
    private NodeInterface nodeInterface;
    private String nodeUrl;
    
    public RMIStorageNodeClient(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }
    
    // Establecer conexión RMI con nodo de almacenamiento
    public boolean connect() {
        try {
            this.nodeInterface = (NodeInterface) Naming.lookup(nodeUrl);
            logger.info("Connected to storage node: " + nodeUrl);
            return true;
        } catch (Exception e) {
            logger.severe("Failed to connect to storage node " + nodeUrl + ": " + e.getMessage());
            return false;
        }
    }
    
    // Cerrar conexión con nodo de almacenamiento
    public void disconnect() {
        this.nodeInterface = null;
        logger.info("Disconnected from storage node: " + nodeUrl);
    }
    
    // Verificar estado de conexión con el nodo
    public boolean isConnected() {
        return this.nodeInterface != null;
    }
    
    // Enviar fragmento de archivo al nodo para almacenamiento
    public boolean storeFileChunk(String fileId, int chunkNumber, byte[] data) {
        if (!isConnected()) {
            logger.warning("Not connected to storage node");
            return false;
        }
        
        try {
            boolean result = nodeInterface.storeFileChunk(fileId, chunkNumber, data);
            if (result) {
                logger.info("Successfully stored chunk " + chunkNumber + " of file " + fileId);
            } else {
                logger.warning("Failed to store chunk " + chunkNumber + " of file " + fileId);
            }
            return result;
        } catch (RemoteException e) {
            logger.severe("RMI error storing file chunk: " + e.getMessage());
            return false;
        }
    }
    
    // Obtener fragmento de archivo desde el nodo
    public byte[] retrieveFileChunk(String fileId, int chunkNumber) {
        if (!isConnected()) {
            logger.warning("Not connected to storage node");
            return null;
        }
        
        try {
            byte[] data = nodeInterface.retrieveFileChunk(fileId, chunkNumber);
            if (data != null) {
                logger.info("Successfully retrieved chunk " + chunkNumber + " of file " + fileId);
            } else {
                logger.warning("Chunk " + chunkNumber + " of file " + fileId + " not found");
            }
            return data;
        } catch (RemoteException e) {
            logger.severe("RMI error retrieving file chunk: " + e.getMessage());
            return null;
        }
    }
    
    // Eliminar fragmento de archivo del nodo
    public boolean deleteFileChunk(String fileId, int chunkNumber) {
        if (!isConnected()) {
            logger.warning("Not connected to storage node");
            return false;
        }
        
        try {
            boolean result = nodeInterface.deleteFileChunk(fileId, chunkNumber);
            if (result) {
                logger.info("Successfully deleted chunk " + chunkNumber + " of file " + fileId);
            } else {
                logger.warning("Failed to delete chunk " + chunkNumber + " of file " + fileId);
            }
            return result;
        } catch (RemoteException e) {
            logger.severe("RMI error deleting file chunk: " + e.getMessage());
            return false;
        }
    }
    
    // Obtener lista de archivos almacenados en el nodo
    public List<String> listStoredFiles() {
        if (!isConnected()) {
            logger.warning("Not connected to storage node");
            return null;
        }
        
        try {
            List<String> files = nodeInterface.listStoredFiles();
            logger.info("Retrieved file list from storage node, count: " + (files != null ? files.size() : 0));
            return files;
        } catch (RemoteException e) {
            logger.severe("RMI error listing stored files: " + e.getMessage());
            return null;
        }
    }
    
    // Consultar estado operativo del nodo
    public String getNodeStatus() {
        if (!isConnected()) {
            logger.warning("Not connected to storage node");
            return "DISCONNECTED";
        }
        
        try {
            return nodeInterface.getNodeStatus();
        } catch (RemoteException e) {
            logger.severe("RMI error getting node status: " + e.getMessage());
            return "ERROR";
        }
    }
    
    // Obtener espacio disponible
    public long getAvailableSpace() {
        if (!isConnected()) {
            return -1;
        }
        
        try {
            return nodeInterface.getAvailableSpace();
        } catch (RemoteException e) {
            logger.severe("RMI error getting available space: " + e.getMessage());
            return -1;
        }
    }
    
    // Obtener espacio usado
    public long getUsedSpace() {
        if (!isConnected()) {
            return -1;
        }
        
        try {
            return nodeInterface.getUsedSpace();
        } catch (RemoteException e) {
            logger.severe("RMI error getting used space: " + e.getMessage());
            return -1;
        }
    }
    
    // Verificar conectividad y respuesta del nodo
    public boolean ping() {
        if (!isConnected()) {
            return false;
        }
        
        try {
            return nodeInterface.ping();
        } catch (RemoteException e) {
            logger.warning("Ping failed to storage node: " + e.getMessage());
            return false;
        }
    }
    
    // Verificar estado de salud y operatividad del nodo
    public boolean isHealthy() {
        if (!isConnected()) {
            return false;
        }
        
        try {
            return nodeInterface.isHealthy();
        } catch (RemoteException e) {
            logger.warning("Health check failed for storage node: " + e.getMessage());
            return false;
        }
    }
    
    // Actualizar información del nodo
    public boolean updateNodeInfo(String status, long availableSpace, long usedSpace) {
        if (!isConnected()) {
            return false;
        }
        
        try {
            nodeInterface.updateNodeInfo(status, availableSpace, usedSpace);
            return true;
        } catch (RemoteException e) {
            logger.severe("RMI error updating node info: " + e.getMessage());
            return false;
        }
    }
    
    // Verificar si tiene un archivo específico
    public boolean hasFile(String fileId, int chunkNumber) {
        if (!isConnected()) {
            return false;
        }
        
        try {
            return nodeInterface.hasFile(fileId, chunkNumber);
        } catch (RemoteException e) {
            logger.severe("RMI error checking file existence: " + e.getMessage());
            return false;
        }
    }
    
    // Obtener la URL del nodo
    public String getNodeUrl() {
        return nodeUrl;
    }
    
    // Replicar chunk a otro nodo
    public boolean replicateChunk(String fileId, int chunkNumber, byte[] data, String targetNodeUrl) {
        if (!isConnected()) {
            return false;
        }
        
        try {
            return nodeInterface.replicateChunk(fileId, chunkNumber, data, targetNodeUrl);
        } catch (RemoteException e) {
            logger.severe("RMI error replicating chunk: " + e.getMessage());
            return false;
        }
    }
}
