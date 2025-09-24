package servidor.aplicacion;

import java.sql.Connection;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import jakarta.xml.ws.Endpoint;

import servidor.aplicacion.config.DatabaseConnection;
import servidor.aplicacion.rmi.FileServiceRMIImpl;
import servidor.aplicacion.rmi.AuthServiceRMIImpl;
import servidor.aplicacion.soap.FileServiceSOAPImpl;
import servidor.aplicacion.soap.AuthServiceSOAPImpl;

/**
 * Servidor de Aplicación Distribuido
 * Inicia servicios SOAP y RMI para Files y Auth
 */
public class App {
    
    private static final String FILE_SOAP_URL = "http://localhost:8080/fileservice";
    private static final String AUTH_SOAP_URL = "http://localhost:8081/authservice";
    private static final String FILE_RMI_SERVICE_NAME = "FileService";
    private static final String AUTH_RMI_SERVICE_NAME = "AuthService";
    private static final int RMI_PORT = 1099;
    
    public static void main(String[] args) {
        System.out.println("=== Servidor de Aplicación Distribuido ===");
        
        // Verificar conexión a base de datos
        testDatabaseConnection();
        
        // Iniciar servidor RMI
        startRMIServer();
        
        // Iniciar servidor SOAP
        startSOAPServer();
        
        System.out.println("\n=== Servidores iniciados exitosamente ===");
        System.out.println("File SOAP: " + FILE_SOAP_URL + "?wsdl");
        System.out.println("Auth SOAP: " + AUTH_SOAP_URL + "?wsdl");
        System.out.println("File RMI: rmi://localhost:" + RMI_PORT + "/" + FILE_RMI_SERVICE_NAME);
        System.out.println("Auth RMI: rmi://localhost:" + RMI_PORT + "/" + AUTH_RMI_SERVICE_NAME);
        System.out.println("\nPresiona Ctrl+C para detener el servidor");
        
        // Mantener el servidor corriendo
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Servidor interrumpido");
        }
    }
    
    private static void testDatabaseConnection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Conexión exitosa a la base de datos!");
            }
        } catch (Exception e) {
            System.err.println("❌ Error de conexión a la base de datos:");
            e.printStackTrace();
        }
    }
    
    private static void startRMIServer() {
        try {
            // Crear el registro RMI
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            
            // Crear implementaciones de los servicios
            FileServiceRMIImpl fileServiceRMI = new FileServiceRMIImpl();
            AuthServiceRMIImpl authServiceRMI = new AuthServiceRMIImpl();
            
            // Registrar los servicios
            registry.bind(FILE_RMI_SERVICE_NAME, fileServiceRMI);
            registry.bind(AUTH_RMI_SERVICE_NAME, authServiceRMI);
            
            System.out.println("✅ Servidor RMI iniciado en puerto " + RMI_PORT);
            System.out.println("  - FileService: " + FILE_RMI_SERVICE_NAME);
            System.out.println("  - AuthService: " + AUTH_RMI_SERVICE_NAME);
            
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar servidor RMI:");
            e.printStackTrace();
        }
    }
    
    private static void startSOAPServer() {
        try {
            // Crear implementaciones de los servicios SOAP
            FileServiceSOAPImpl fileServiceSOAP = new FileServiceSOAPImpl();
            AuthServiceSOAPImpl authServiceSOAP = new AuthServiceSOAPImpl();
            
            // Publicar los servicios
            Endpoint fileEndpoint = Endpoint.publish(FILE_SOAP_URL, fileServiceSOAP);
            Endpoint authEndpoint = Endpoint.publish(AUTH_SOAP_URL, authServiceSOAP);
            
            if (fileEndpoint.isPublished()) {
                System.out.println("✅ File SOAP Service iniciado en " + FILE_SOAP_URL);
            }
            
            if (authEndpoint.isPublished()) {
                System.out.println("✅ Auth SOAP Service iniciado en " + AUTH_SOAP_URL);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar servidor SOAP:");
            e.printStackTrace();
        }
    }
}
