package servidor.aplicacion.rmi.nodes;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Logger;

public class StorageNodeServer {
    private static final Logger logger = Logger.getLogger(StorageNodeServer.class.getName());

    private final String nodeName;
    private final int port;
    private final String storagePath;
    private Registry registry;
    private StorageNode storageNode;

    public StorageNodeServer(String nodeName, int port, String storagePath) {
        this.nodeName = nodeName;
        this.port = port;
        this.storagePath = storagePath;
    }

    public void start() {
        try {
            // Crear el registro RMI en el puerto específico del nodo
            registry = LocateRegistry.createRegistry(port);
            logger.info("RMI Registry created on port " + port);

            // Crear la instancia del nodo de almacenamiento
            storageNode = new StorageNode(nodeName, storagePath);

            // Registrar el nodo en el registro RMI
            registry.bind("StorageNode", storageNode);

            logger.info("Storage Node '" + nodeName + "' started successfully on port " + port);
            logger.info("Storage path: " + storagePath);
            logger.info("RMI URL: rmi://localhost:" + port + "/StorageNode");

        } catch (Exception e) {
            logger.severe("Failed to start Storage Node '" + nodeName + "': " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to start storage node", e);
        }
    }

    public void stop() {
        try {
            if (registry != null && storageNode != null) {
                registry.unbind("StorageNode");
                logger.info("Storage Node '" + nodeName + "' stopped");
            }
        } catch (Exception e) {
            logger.warning("Error stopping Storage Node '" + nodeName + "': " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java StorageNodeServer <nodeName> <port> <storagePath> [bindIP]");
            System.exit(1);
        }

        String nodeName = args[0];
        int port = Integer.parseInt(args[1]);
        String storagePath = args[2];
        String bindIP = args.length > 3 ? args[3] : "0.0.0.0"; // Escuchar en todas las interfaces

        // Configurar RMI para usar IP específica
        System.setProperty("java.rmi.server.hostname", bindIP);
        StorageNodeServer server = new StorageNodeServer(nodeName, port, storagePath);

        try {
            server.start();

            // Configurar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nCerrando nodo...");
                server.stop();
            }));

            System.out.println("Nodo " + nodeName + " puerto " + port + " - OK");
            System.out.println("Listo para peticiones");

            // Mantener el servidor corriendo
            Thread.currentThread().join();

        } catch (InterruptedException e) {
            System.out.println("Nodo interrumpido");
        } catch (Exception e) {
            System.err.println("Error running storage node: " + e.getMessage());
            e.printStackTrace();
        }
    }
}