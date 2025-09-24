package servidor.aplicacion.client;

import servidor.aplicacion.dto.FileDTO;
import servidor.aplicacion.rmi.FileServiceRMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class RMITestClient {
    
    public static void main(String[] args) {
        try {
            // Conectar al registro RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            FileServiceRMI fileService = (FileServiceRMI) registry.lookup("FileService");
            
            System.out.println("=== Probando Cliente RMI ===");
            
            // Ping al servidor
            System.out.println("Ping: " + fileService.ping());
            
            // Crear directorio
            System.out.println("\n1. Creando directorio...");
            FileDTO directory = fileService.createDirectory("TestDir", null, 1L);
            System.out.println("Directorio creado: " + directory);
            
            // Subir archivo
            System.out.println("\n2. Subiendo archivo...");
            String fileContent = "Hello World from RMI!";
            FileDTO file = fileService.uploadFile("test.txt", directory.getId(), 1L, 1, fileContent.getBytes());
            System.out.println("Archivo subido: " + file);
            
            // Listar archivos
            System.out.println("\n3. Listando archivos del directorio...");
            List<FileDTO> files = fileService.listFiles(directory.getId(), 1L);
            files.forEach(f -> System.out.println("  - " + f));
            
            // Obtener info del archivo
            System.out.println("\n4. Obteniendo info del archivo...");
            FileDTO fileInfo = fileService.getFileInfo(file.getId(), 1L);
            System.out.println("Info del archivo: " + fileInfo);
            
            // Listar todos los archivos del usuario
            System.out.println("\n5. Listando todos los archivos del usuario...");
            List<FileDTO> userFiles = fileService.getUserFiles(1L);
            userFiles.forEach(f -> System.out.println("  - " + f));
            
            // Descargar archivo (por ahora retorna array vacío)
            System.out.println("\n6. Descargando archivo...");
            byte[] downloadedData = fileService.downloadFile(file.getId(), 1L);
            System.out.println("Datos descargados: " + downloadedData.length + " bytes");
            
            System.out.println("\n✅ Todas las pruebas RMI completadas exitosamente!");
            
        } catch (Exception e) {
            System.err.println("❌ Error en cliente RMI:");
            e.printStackTrace();
        }
    }
}