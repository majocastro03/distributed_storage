package servidor.aplicacion;

import java.sql.Connection;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import jakarta.xml.ws.Endpoint;

import servidor.aplicacion.config.DatabaseConnection;
import servidor.aplicacion.rmi.nodes.StorageNodesLauncher;
import servidor.aplicacion.rmi.services.AuthServiceRMI;
import servidor.aplicacion.rmi.services.FileServiceRMI;
import servidor.aplicacion.services.NodeIntegratedService;
import servidor.aplicacion.soap.services.AuthServiceSOAP;
import servidor.aplicacion.soap.services.FileServiceSOAP;

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

    private static NodeIntegratedService nodeService;
    private static StorageNodesLauncher nodesLauncher;

    public static void main(String[] args) {
        System.out.println("=== Servidor Distribuido ===");
        System.out.println("Iniciando en localhost");
        System.out.println("Puerto RMI: " + RMI_PORT);

        // Verificar conexión a base de datos
        testDatabaseConnection();

        // Iniciar nodos de almacenamiento distribuido
        startStorageNodes();

        // Inicializar sistema de monitoreo de nodos
        initializeNodeSystem();

        // Iniciar servidor RMI para servicios remotos
        startRMIServer();

        // Iniciar servidor SOAP para testing con Postman
        startSOAPServer();

        System.out.println("\n=== Servidor Iniciado ===");
        System.out.println("Estado: Funcionando correctamente");
        System.out.println("SOAP - File: " + FILE_SOAP_URL + "?wsdl");
        System.out.println("SOAP - Auth: " + AUTH_SOAP_URL + "?wsdl");
        System.out.println("RMI - Puerto: " + RMI_PORT);

        // Mostrar estado de nodos
        showNodeStatistics();

        System.out.println("\nPresiona Ctrl+C para detener");

        // Configurar cierre controlado del servidor y limpieza de recursos
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nCerrando servidor de aplicacion...");
            if (nodeService != null) {
                nodeService.shutdown();
            }
            if (nodesLauncher != null) {
                nodesLauncher.stopAllNodes();
            }
            System.out.println("Servidor de aplicacion detenido correctamente.");
        }));

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
                System.out.println("Base de datos: OK");
            }
        } catch (Exception e) {
            System.err.println("Error de conexión a la base de datos:");
            e.printStackTrace();
        }
    }

    private static void startRMIServer() {
        try {
            // Crear el registro RMI
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);

            // Crear implementaciones de los servicios
            FileServiceRMI fileServiceRMI = new FileServiceRMI();
            AuthServiceRMI authServiceRMI = new AuthServiceRMI();

            // Registrar los servicios
            registry.bind(FILE_RMI_SERVICE_NAME, fileServiceRMI);
            registry.bind(AUTH_RMI_SERVICE_NAME, authServiceRMI);

        } catch (Exception e) {
            System.err.println("Error al iniciar servidor RMI:");
            e.printStackTrace();
        }
    }

    private static void startSOAPServer() {
        try {
            // Crear implementaciones de los servicios SOAP
            FileServiceSOAP fileServiceSOAP = new FileServiceSOAP();
            AuthServiceSOAP authServiceSOAP = new AuthServiceSOAP();

            // Publicar los servicios
            Endpoint fileEndpoint = Endpoint.publish(FILE_SOAP_URL, fileServiceSOAP);
            Endpoint authEndpoint = Endpoint.publish(AUTH_SOAP_URL, authServiceSOAP);

            if (fileEndpoint.isPublished()) {
                System.out.println("SOAP File: OK");
            }

            if (authEndpoint.isPublished()) {
                System.out.println("SOAP Auth: OK");
            }

        } catch (Exception e) {
            System.err.println("Error al iniciar servidor SOAP:");
            e.printStackTrace();
        }
    }

    private static void startStorageNodes() {
        try {
            System.out.println("Iniciando nodos...");
            nodesLauncher = new StorageNodesLauncher();

            // Iniciar nodos en un hilo separado para no bloquear el hilo principal
            Thread nodesThread = new Thread(() -> {
                try {
                    nodesLauncher.startAllNodes();
                } catch (Exception e) {
                    System.err.println("Error iniciando nodos de almacenamiento: " + e.getMessage());
                }
            });
            nodesThread.setDaemon(false); // No daemon para que mantenga el proceso vivo
            nodesThread.start();

            // Dar tiempo a los nodos para inicializarse
            Thread.sleep(3000);
            System.out.println("Nodos iniciados");

        } catch (Exception e) {
            System.err.println("Error al iniciar nodos de almacenamiento:");
            e.printStackTrace();
        }
    }

    private static void initializeNodeSystem() {
        try {
            System.out.println("Inicializando sistema de nodos...");
            nodeService = new NodeIntegratedService();
            nodeService.registerSystemNodes();
            System.out.println("Sistema de nodos inicializado");
        } catch (Exception e) {
            System.err.println("Error al inicializar sistema de nodos:");
            e.printStackTrace();
        }
    }

    private static void showNodeStatistics() {
        try {
            if (nodeService != null) {
                List<servidor.aplicacion.model.Node> onlineNodes = nodeService.getOnlineNodes();
                System.out.println("\nNodos: " + onlineNodes.size() + " online");

                nodeService.checkNodeHealth();

                for (servidor.aplicacion.model.Node node : onlineNodes) {
                    System.out.println("Nodo " + node.getPort() + " - responde OK");
                }
            }
        } catch (Exception e) {
            System.err.println("Error verificando nodos: " + e.getMessage());
        }
    }

    // Obtener servicio de nodos para consultas desde otros componentes del sistema
    public static NodeIntegratedService getNodeService() {
        return nodeService;
    }
}
