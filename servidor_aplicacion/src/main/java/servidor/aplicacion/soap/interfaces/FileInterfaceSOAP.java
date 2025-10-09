package servidor.aplicacion.soap.interfaces;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import servidor.aplicacion.dto.FileDTO;
import servidor.aplicacion.dto.DirectoryListDTO;
import java.util.List;

@WebService(name = "FileInterfaceSOAP", targetNamespace = "http://soap.aplicacion.servidor/")
public interface FileInterfaceSOAP {
    
    @WebMethod
    FileDTO uploadFile(@WebParam(name = "name") String name,
                      @WebParam(name = "parentId") Long parentId,
                      @WebParam(name = "ownerId") Long ownerId,
                      @WebParam(name = "type") Integer type,
                      @WebParam(name = "data") String data) throws Exception;

    @WebMethod
    List<FileDTO> uploadFiles(@WebParam(name = "files") List<FileDTO> files) throws Exception;

    @WebMethod
    String downloadFile(@WebParam(name = "fileId") Long fileId,
                       @WebParam(name = "userId") Long userId) throws Exception;

    @WebMethod
    List<String> downloadFiles(@WebParam(name = "fileIds") List<Long> fileIds,
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
    List<FileDTO> createDirectories(@WebParam(name = "directories") DirectoryListDTO directories) throws Exception;

    @WebMethod
    FileDTO getFileInfo(@WebParam(name = "fileId") Long fileId,
                       @WebParam(name = "userId") Long userId) throws Exception;

    @WebMethod
    List<FileDTO> getUserFiles(@WebParam(name = "userId") Long userId);
}
