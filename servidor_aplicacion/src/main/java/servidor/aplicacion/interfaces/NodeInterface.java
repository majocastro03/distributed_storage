package servidor.aplicacion.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

// funcional, no tocar que por fin funcionó :)
public interface NodeInterface extends Remote {

    // Operaciones de archivos
    boolean storeFileChunk(String fileId, int chunkNumber, byte[] data) throws RemoteException;

    byte[] retrieveFileChunk(String fileId, int chunkNumber) throws RemoteException;

    boolean deleteFileChunk(String fileId, int chunkNumber) throws RemoteException;

    List<String> listStoredFiles() throws RemoteException;

    // Operaciones de estado del nodo
    String getNodeStatus() throws RemoteException;

    long getAvailableSpace() throws RemoteException;

    long getUsedSpace() throws RemoteException;

    boolean ping() throws RemoteException;

    // Operaciones de gestión
    void updateNodeInfo(String status, long availableSpace, long usedSpace) throws RemoteException;

    boolean isHealthy() throws RemoteException;

    // Sincronización
    List<String> getFileList() throws RemoteException;

    boolean hasFile(String fileId, int chunkNumber) throws RemoteException;

    // Replicación
    boolean replicateChunk(String fileId, int chunkNumber, byte[] data, String targetNodeUrl) throws RemoteException;
}
