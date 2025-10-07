package servidor.aplicacion.rmi.services;

import servidor.aplicacion.dao.FileDAO;
import servidor.aplicacion.dto.FileDTO;
import servidor.aplicacion.model.File;
import servidor.aplicacion.rmi.interfaces.FileInterfaceRMI;
import servidor.aplicacion.services.FileService;
import servidor.aplicacion.util.FileConverter;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class FileServiceRMI extends UnicastRemoteObject implements FileInterfaceRMI {
    
    private final FileService fileService;
    
    public FileServiceRMI() throws RemoteException {
        super();
        this.fileService = new FileService(new FileDAO());
    }
    
    @Override
    public FileDTO uploadFile(String name, Long parentId, Long ownerId, Integer type, byte[] data) 
            throws RemoteException, Exception {
        try {
            File file = new File(name, parentId, ownerId, type, (long) data.length);
            File uploadedFile = fileService.uploadFile(file, data, ownerId);
            return FileConverter.toDTO(uploadedFile);
        } catch (Exception e) {
            throw new RemoteException("Error uploading file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] downloadFile(Long fileId, Long userId) throws RemoteException, Exception {
        try {
            return fileService.downloadFile(fileId, userId);
        } catch (Exception e) {
            throw new RemoteException("Error downloading file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<FileDTO> listFiles(Long parentId, Long userId) throws RemoteException {
        try {
            List<File> files = fileService.listFiles(parentId, userId);
            return FileConverter.toDTOList(files);
        } catch (Exception e) {
            throw new RemoteException("Error listing files: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteFile(Long fileId, Long userId) throws RemoteException, Exception {
        try {
            fileService.deleteFile(fileId, userId);
        } catch (Exception e) {
            throw new RemoteException("Error deleting file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public FileDTO createDirectory(String name, Long parentId, Long ownerId) 
            throws RemoteException, Exception {
        try {
            File directory = fileService.createDirectory(name, parentId, ownerId);
            return FileConverter.toDTO(directory);
        } catch (Exception e) {
            throw new RemoteException("Error creating directory: " + e.getMessage(), e);
        }
    }
    
    @Override
    public FileDTO getFileInfo(Long fileId, Long userId) throws RemoteException, Exception {
        try {
            File file = fileService.getFileInfo(fileId, userId);
            return FileConverter.toDTO(file);
        } catch (Exception e) {
            throw new RemoteException("Error getting file info: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<FileDTO> getUserFiles(Long userId) throws RemoteException {
        try {
            List<File> files = fileService.getUserFiles(userId);
            return FileConverter.toDTOList(files);
        } catch (Exception e) {
            throw new RemoteException("Error getting user files: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String ping() throws RemoteException {
        return "RMI Server is alive - " + System.currentTimeMillis();
    }
}