package servidor.aplicacion.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import servidor.aplicacion.dto.FileDTO;
import java.util.List;

@WebService(name = "FileService", targetNamespace = "http://soap.aplicacion.servidor/")
public interface FileServiceSOAP {
    
    @WebMethod
    FileDTO uploadFile(@WebParam(name = "name") String name,
                      @WebParam(name = "parentId") Long parentId,
                      @WebParam(name = "ownerId") Long ownerId,
                      @WebParam(name = "type") Integer type,
                      @WebParam(name = "data") String data) throws Exception;
    
    @WebMethod
    String downloadFile(@WebParam(name = "fileId") Long fileId,
                       @WebParam(name = "userId") Long userId) throws Exception;
    
    @WebMethod
    List<FileDTO> listFiles(@WebParam(name = "parentId") Long parentId,
                           @WebParam(name = "userId") Long userId);
    
    @WebMethod
    void deleteFile(@WebParam(name = "fileId") Long fileId,
                   @WebParam(name = "userId") Long userId) throws Exception;
    
    @WebMethod
    FileDTO createDirectory(@WebParam(name = "name") String name,
                           @WebParam(name = "parentId") Long parentId,
                           @WebParam(name = "ownerId") Long ownerId) throws Exception;
    
    @WebMethod
    FileDTO getFileInfo(@WebParam(name = "fileId") Long fileId,
                       @WebParam(name = "userId") Long userId) throws Exception;
    
    @WebMethod
    List<FileDTO> getUserFiles(@WebParam(name = "userId") Long userId);
}
