package servidor.aplicacion.rmi.interfaces;

import servidor.aplicacion.dto.FileDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FileInterfaceRMI extends Remote {
    
    FileDTO uploadFile(String name, Long parentId, Long ownerId, Integer type, byte[] data) 
            throws RemoteException, Exception;
    
    byte[] downloadFile(Long fileId, Long userId) 
            throws RemoteException, Exception;
    
    List<FileDTO> listFiles(Long parentId, Long userId) 
            throws RemoteException;
    
    void deleteFile(Long fileId, Long userId) 
            throws RemoteException, Exception;
    
    FileDTO createDirectory(String name, Long parentId, Long ownerId) 
            throws RemoteException, Exception;
    
    FileDTO getFileInfo(Long fileId, Long userId) 
            throws RemoteException, Exception;
    
    List<FileDTO> getUserFiles(Long userId) 
            throws RemoteException;
    
    String ping() throws RemoteException;
}