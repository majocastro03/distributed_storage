package servidor.aplicacion.soap;

import jakarta.jws.WebService;
import servidor.aplicacion.dao.FileDAO;
import servidor.aplicacion.dto.FileDTO;
import servidor.aplicacion.model.File;
import servidor.aplicacion.service.FileService;
import servidor.aplicacion.util.FileConverter;
import java.util.Base64;
import java.util.List;

@WebService(endpointInterface = "servidor.aplicacion.soap.FileServiceSOAP")
public class FileServiceSOAPImpl implements FileServiceSOAP {
    
    private final FileService fileService;
    
    public FileServiceSOAPImpl() {
        this.fileService = new FileService(new FileDAO());
    }
    
    @Override
    public FileDTO uploadFile(String name, Long parentId, Long ownerId, Integer type, String data) throws Exception {
        // Decodificar los datos base64
        byte[] fileData = data != null ? Base64.getDecoder().decode(data) : new byte[0];
        
        // Crear el archivo
        File file = new File(name, parentId, ownerId, type, (long) fileData.length);
        
        // Subir el archivo
        File uploadedFile = fileService.uploadFile(file, fileData, ownerId);
        
        // Convertir a DTO
        return FileConverter.toDTO(uploadedFile);
    }
    
    @Override
    public String downloadFile(Long fileId, Long userId) throws Exception {
        byte[] data = fileService.downloadFile(fileId, userId);
        return Base64.getEncoder().encodeToString(data);
    }
    
    @Override
    public List<FileDTO> listFiles(Long parentId, Long userId) {
        List<File> files = fileService.listFiles(parentId, userId);
        return FileConverter.toDTOList(files);
    }
    
    @Override
    public void deleteFile(Long fileId, Long userId) throws Exception {
        fileService.deleteFile(fileId, userId);
    }
    
    @Override
    public FileDTO createDirectory(String name, Long parentId, Long ownerId) throws Exception {
        File directory = fileService.createDirectory(name, parentId, ownerId);
        return FileConverter.toDTO(directory);
    }
    
    @Override
    public FileDTO getFileInfo(Long fileId, Long userId) throws Exception {
        File file = fileService.getFileInfo(fileId, userId);
        return FileConverter.toDTO(file);
    }
    
    @Override
    public List<FileDTO> getUserFiles(Long userId) {
        List<File> files = fileService.getUserFiles(userId);
        return FileConverter.toDTOList(files);
    }
}
