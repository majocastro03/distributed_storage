package servidor.aplicacion.soap.services;
import jakarta.jws.WebService;
import servidor.aplicacion.dao.FileDAO;
import servidor.aplicacion.dao.FileChunkDAO;
import servidor.aplicacion.dto.FileDTO;
import servidor.aplicacion.manager.NodeManager;
import servidor.aplicacion.model.File;
import servidor.aplicacion.services.DistributedFileService;
import servidor.aplicacion.soap.interfaces.FileInterfaceSOAP;
import servidor.aplicacion.util.FileConverter;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

//Implementación SOAP que usa el sistema distribuido de archivos
// Permite testing con Postman y herramientas SOAP
@WebService(endpointInterface = "servidor.aplicacion.soap.interfaces.FileInterfaceSOAP")
public class FileServiceSOAP implements FileInterfaceSOAP {

    
    private static final Logger logger = Logger.getLogger(FileServiceSOAP.class.getName());
    private final DistributedFileService distributedFileService;
    
    public FileServiceSOAP() {
        // Inicializar el servicio distribuido con todas las dependencias
        FileDAO fileDAO = new FileDAO();
        FileChunkDAO fileChunkDAO = new FileChunkDAO();
        NodeManager nodeManager = new NodeManager();
        
        this.distributedFileService = new DistributedFileService(fileDAO, fileChunkDAO, nodeManager);
    }
    
    @Override
    public FileDTO uploadFile(String name, Long parentId, Long ownerId, Integer type, String data) throws Exception {
        // Log de debugging detallado
        logger.info("=== SOAP Upload Debug ===");
        logger.info("Raw name parameter: '" + name + "'");
        if (name == null) {
            logger.warning("El parámetro 'name' llegó como null");
        } else {
            logger.warning("El parámetro 'name' tiene longitud: " + name.length());
            for (int i = 0; i < name.length(); i++) {
                logger.warning("name.charAt(" + i + ") = '" + name.charAt(i) + "' (int: " + (int)name.charAt(i) + ")");
            }
        }
        logger.info("Raw parentId parameter: " + parentId);
        logger.info("Raw ownerId parameter: " + ownerId);
        logger.info("Raw type parameter: " + type);
        logger.info("Raw data length: " + (data != null ? data.length() : "null"));
        logger.info("========================");
        
        try {
            // Validación más flexible
            String fileName = (name != null && !name.trim().isEmpty()) ? name.trim() : "archivo_sin_nombre.dat";
            Long finalOwnerId = (ownerId != null) ? ownerId : 1L;
            Long finalParentId = (parentId != null) ? parentId : 1L;
            Integer finalType = (type != null) ? type : 1;
            
            logger.info("Processing file: " + fileName + " for owner: " + finalOwnerId);
            
            // Manejar datos vacíos
            byte[] fileData = new byte[0];
            if (data != null && !data.trim().isEmpty()) {
                try {
                    fileData = Base64.getDecoder().decode(data.trim());
                    logger.info("File data decoded: " + fileData.length + " bytes");
                } catch (Exception e) {
                    logger.warning("Base64 decode failed, using empty data: " + e.getMessage());
                }
            } else {
                logger.info("No data provided, creating empty file");
            }
            
            // Crear el archivo
            File file = new File(fileName, finalParentId, finalOwnerId, finalType, (long) fileData.length);
            
            // Usar el servicio distribuido
            File uploadedFile = distributedFileService.uploadFile(file, fileData, finalOwnerId);
            
            // Convertir a DTO
            FileDTO result = FileConverter.toDTO(uploadedFile);
            logger.info("Archivo subido exitosamente: ID = " + result.getId());
            return result;
            
        } catch (Exception e) {
            logger.severe("Error en subida de archivo: " + e.getMessage());
            e.printStackTrace();
            
            // Crear una respuesta de error simple
            FileDTO errorResult = new FileDTO();
            errorResult.setId(-1L);
            errorResult.setName("ERROR: " + e.getMessage());
            return errorResult;
        }
    }

        @Override
        public List<FileDTO> uploadFiles(List<FileDTO> files) throws Exception {
            List<FileDTO> result = new java.util.ArrayList<>();
            if (files != null) {
                for (FileDTO fileDTO : files) {
                    FileDTO uploaded = uploadFile(
                        fileDTO.getName(),
                        fileDTO.getParentId(),
                        fileDTO.getOwnerId(),
                        fileDTO.getType(),
                        fileDTO.getData()
                    );
                    result.add(uploaded);
                }
            }
            return result;
        }
    
    @Override
    public String downloadFile(Long fileId, Long userId) throws Exception {
        logger.info("SOAP Download request: fileId=" + fileId + ", userId=" + userId);
        
        try {
            byte[] data = distributedFileService.downloadFile(fileId, userId);
            String encoded = Base64.getEncoder().encodeToString(data);
            logger.info("SOAP Download completado: " + data.length + " bytes");
            return encoded;
            
        } catch (Exception e) {
            logger.severe("Error en SOAP download: " + e.getMessage());
            throw new Exception("Error downloading file: " + e.getMessage());
        }
    }

        @Override
        public List<String> downloadFiles(List<Long> fileIds, Long userId) throws Exception {
            List<String> result = new java.util.ArrayList<>();
            if (fileIds != null) {
                for (Long fileId : fileIds) {
                    String fileData = downloadFile(fileId, userId);
                    result.add(fileData);
                }
            }
            return result;
        }
    
    @Override
    public List<FileDTO> listFiles(Long parentId, Long userId) {
        logger.info("SOAP List files request: parentId=" + parentId + ", userId=" + userId);
        
        try {
            List<File> files = distributedFileService.listFiles(parentId, userId);
            List<FileDTO> result = FileConverter.toDTOList(files);
            logger.info("SOAP List files: " + result.size() + " archivos encontrados");
            return result;
            
        } catch (Exception e) {
            logger.severe("Error en SOAP list files: " + e.getMessage());
            return FileConverter.toDTOList(List.of());
        }
    }
    
    @Override
    public void deleteFile(Long fileId, Long userId) throws Exception {
        logger.info("SOAP Delete request: fileId=" + fileId + ", userId=" + userId);
        
        try {
            distributedFileService.deleteFile(fileId, userId);
            logger.info("SOAP Delete completado exitosamente");
            
        } catch (Exception e) {
            logger.severe("Error en SOAP delete: " + e.getMessage());
            throw new Exception("Error deleting file: " + e.getMessage());
        }
    }
    
    @Override
    public FileDTO createDirectory(String name, Long parentId, Long ownerId) throws Exception {
        logger.info("SOAP Create directory request: " + name + " (owner: " + ownerId + ")");
        
        try {
            File directory = distributedFileService.createDirectory(name, parentId, ownerId);
            FileDTO result = FileConverter.toDTO(directory);
            logger.info("SOAP Directory creado exitosamente: " + result.getId());
            return result;
            
        } catch (Exception e) {
            logger.severe("Error en SOAP create directory: " + e.getMessage());
            throw new Exception("Error creating directory: " + e.getMessage());
        }
    }
    
    @Override
    public FileDTO getFileInfo(Long fileId, Long userId) throws Exception {
        logger.info("SOAP Get file info request: fileId=" + fileId + ", userId=" + userId);
        
        try {
            File file = distributedFileService.getFileInfo(fileId, userId);
            FileDTO result = FileConverter.toDTO(file);
            logger.info("SOAP File info obtenido exitosamente");
            return result;
            
        } catch (Exception e) {
            logger.severe("Error en SOAP get file info: " + e.getMessage());
            throw new Exception("Error getting file info: " + e.getMessage());
        }
    }
    
    @Override
    public List<FileDTO> getUserFiles(Long userId) {
        logger.info("SOAP Get user files request: userId=" + userId);
        
        try {
            List<File> files = distributedFileService.getUserFiles(userId);
            List<FileDTO> result = FileConverter.toDTOList(files);
            logger.info("SOAP User files: " + result.size() + " archivos del usuario");
            return result;
            
        } catch (Exception e) {
            logger.severe("Error en SOAP get user files: " + e.getMessage());
            return FileConverter.toDTOList(List.of());
        }
    }
}
