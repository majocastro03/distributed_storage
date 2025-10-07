package servidor.aplicacion.rmi.nodes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class StorageNodesLauncher {
    private static final Logger logger = Logger.getLogger(StorageNodesLauncher.class.getName());

    // Configuración de nodos por defecto
    private static final NodeConfig[] DEFAULT_NODES = {
            new NodeConfig("Node1", 1100, "./storage/node1"),
            new NodeConfig("Node2", 1101, "./storage/node2"),
            new NodeConfig("Node3", 1102, "./storage/node3")
    };

    private final List<StorageNodeServer> activeNodes = new ArrayList<>();

    public void startAllNodes() {
        // Crear directorios de almacenamiento si no existen
        for (NodeConfig config : DEFAULT_NODES) {
            createStorageDirectory(config.storagePath);
        }

        // Iniciar cada nodo en un hilo separado
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (NodeConfig config : DEFAULT_NODES) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    StorageNodeServer server = new StorageNodeServer(
                            config.nodeName,
                            config.port,
                            config.storagePath);

                    synchronized (activeNodes) {
                        activeNodes.add(server);
                    }

                    server.start();

                } catch (Exception e) {
                    logger.severe("Fallo en " + config.nodeName + ": " + e.getMessage());
                }
            });

            futures.add(future);

            // Pequeña pausa entre inicios de nodos
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Esperar a que todos los nodos estén iniciados
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    System.out.println("\nNodos: " + DEFAULT_NODES.length + " iniciados");
                    for (NodeConfig config : DEFAULT_NODES) {
                        System.out.println(config.nodeName + " puerto " + config.port + " - OK");
                    }
                    System.out.println("Estado: Listos para peticiones\n");
                });
    }

    public void stopAllNodes() {
        synchronized (activeNodes) {
            for (StorageNodeServer server : activeNodes) {
                try {
                    server.stop();
                } catch (Exception e) {
                    logger.warning("Error al detener nodo: " + e.getMessage());
                }
            }
            activeNodes.clear();
        }
    }

    private void createStorageDirectory(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (created) {
                    logger.info("Directorio de almacenamiento creado: " + path);
                } else {
                    logger.warning("Error al crear directorio de almacenamiento: " + path);
                }
            }
        } catch (Exception e) {
            logger.warning("Error al crear directorio de almacenamiento " + path + ": " + e.getMessage());
        }
    }

    // Clase interna para configuración de nodos
    private static class NodeConfig {
        final String nodeName;
        final int port;
        final String storagePath;

        NodeConfig(String nodeName, int port, String storagePath) {
            this.nodeName = nodeName;
            this.port = port;
            this.storagePath = storagePath;
        }
    }

    public static void main(String[] args) {
        StorageNodesLauncher launcher = new StorageNodesLauncher();

        try {
            launcher.startAllNodes();

            // Configurar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nCerrando nodos...");
                launcher.stopAllNodes();
            }));

            System.out.println("Nodos operativos");
            System.out.println("Ctrl+C para detener");

            // Mantener el proceso corriendo
            Thread.currentThread().join();

        } catch (InterruptedException e) {
            System.out.println("Nodos interrumpidos");
        } catch (Exception e) {
            System.err.println("Error in storage nodes launcher: " + e.getMessage());
            e.printStackTrace();
        }
    }
}