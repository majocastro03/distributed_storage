package backend.cliente.controller;

import backend.cliente.soap.SoapClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import servidor.aplicacion.dto.FileDTO;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final SoapClients soapClients;

    public FileController(@Value("${soap.auth.wsdl}") String authWsdl,
                          @Value("${soap.file.wsdl}") String fileWsdl) {
        this.soapClients = new SoapClients(authWsdl, fileWsdl);
    }

    @PostMapping("/upload")
    public ResponseEntity<FileDTO> upload(
            @RequestParam("name") String name,
            @RequestParam(name = "parentId", required = false) Long parentId,
            @RequestParam(name = "ownerId") Long ownerId,
            @RequestParam(name = "type") Integer type,
            @RequestParam(name = "data") String data) throws Exception {
        
        var filePort = soapClients.createFilePort();
        FileDTO created = filePort.uploadFile(name, parentId, ownerId, type, data);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileDTO>> listFiles(
            @RequestParam(name = "parentId", required = false) Long parentId,
            @RequestParam(name = "userId") Long userId) throws Exception {
        
        var filePort = soapClients.createFilePort();
        List<FileDTO> files = filePort.listFiles(parentId, userId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<String> download(
            @PathVariable("fileId") Long fileId,
            @RequestParam("userId") Long userId) throws Exception {
        
        var filePort = soapClients.createFilePort();
        String data = filePort.downloadFile(fileId, userId);
        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(
            @PathVariable("fileId") Long fileId,
            @RequestParam("userId") Long userId) throws Exception {
        
        var filePort = soapClients.createFilePort();
        filePort.deleteFile(fileId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mkdir")
    public ResponseEntity<FileDTO> createDir(
            @RequestParam("name") String name,
            @RequestParam(name = "parentId", required = false) Long parentId,
            @RequestParam("ownerId") Long ownerId) throws Exception {
        
        var filePort = soapClients.createFilePort();
        FileDTO directory = filePort.createDirectory(name, parentId, ownerId);
        return ResponseEntity.ok(directory);
    }
}