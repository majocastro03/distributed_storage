package servidor.aplicacion.manager;

import servidor.aplicacion.dao.NodeDAO;
import servidor.aplicacion.interfaces.NodeInterface;
import servidor.aplicacion.model.Node;

import java.rmi.Naming;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

// NO TOCAR, YA FUNCIONA BIEN!!!!!!!
public class NodeManager {
    private static final Logger logger = Logger.getLogger(NodeManager.class.getName());
    private final NodeDAO nodeDAO;
    private final ScheduledExecutorService scheduler;
    private static final int HEARTBEAT_TIMEOUT_MINUTES = 5;

    public NodeManager() {
        this.nodeDAO = new NodeDAO();
        this.scheduler = Executors.newScheduledThreadPool(2);

        // Iniciar el monitoreo de nodos
        startNodeHealthMonitoring();
    }

    // Registrar un nuevo nodo
    public Node registerNode(String ip, int port) {
        try {
            // Verificar si el nodo ya existe
            if (nodeDAO.nodeExists(ip, port)) {
                logger.warning("Nodo ya existe: " + ip + ":" + port);
                return nodeDAO.getNodeByIpAndPort(ip, port);
            }

            Node node = new Node(ip, port);
            Node registeredNode = nodeDAO.registerNode(node);

            logger.info("Nodo registrado exitosamente: " + ip + ":" + port + " con ID: " + registeredNode.getId());
            return registeredNode;

        } catch (Exception e) {
            logger.severe("Error al registrar nodo " + ip + ":" + port + ": " + e.getMessage());
            throw new RuntimeException("Error al registrar nodo: " + ip + ":" + port, e);
        }
    }

    // Obtener todos los nodos online
    public List<Node> getOnlineNodes() {
        return nodeDAO.getOnlineNodes();
    }

    // Obtener todos los nodos
    public List<Node> getAllNodes() {
        return nodeDAO.getAllNodes();
    }

    // Seleccionar un nodo para operaciones
    public Node selectNodeForOperation() {
        List<Node> onlineNodes = getOnlineNodes();

        if (onlineNodes.isEmpty()) {
            logger.warning("No hay nodos online disponibles");
            return null;
        }

        // Filtrar nodos válidos para almacenamiento (puertos 1100-1199)
        List<Node> storageNodes = onlineNodes.stream()
                .filter(node -> node.getPort() >= 1100 && node.getPort() <= 1199)
                .toList();

        if (storageNodes.isEmpty()) {
            logger.warning("No hay nodos de almacenamiento válidos disponibles (puertos 1100-1199)");
            return null;
        }

        Node selectedNode = storageNodes.get(0);
        logger.info("Nodo de almacenamiento seleccionado: " + selectedNode.getIp() + ":" + selectedNode.getPort());
        return selectedNode;
    }

    public boolean updateNodeHeartbeat(Long nodeId) {
        try {
            boolean updated = nodeDAO.updateHeartbeat(nodeId);
            if (updated) {
                logger.fine("Heartbeat actualizado para el nodo ID: " + nodeId);
            }
            return updated;
        } catch (Exception e) {
            logger.warning("Error al actualizar heartbeat para el nodo ID " + nodeId + ": " + e.getMessage());
            return false;
        }
    }

    // Marcar un nodo como offline
    public boolean markNodeAsOffline(Long nodeId) {
        try {
            return nodeDAO.updateNodeStatus(nodeId, Node.STATUS_OFFLINE, LocalDateTime.now());
        } catch (Exception e) {
            logger.warning("Error al marcar nodo como offline: " + e.getMessage());
            return false;
        }
    }

    // Marcar un nodo como online
    public boolean markNodeAsOnline(Long nodeId) {
        try {
            return nodeDAO.updateNodeStatus(nodeId, Node.STATUS_ONLINE, LocalDateTime.now());
        } catch (Exception e) {
            logger.warning("Error al marcar nodo como online: " + e.getMessage());
            return false;
        }
    }

    // Verificar si un nodo está disponible
    public boolean isNodeAvailable(Node node) {
        if (!node.isOnline()) {
            return false;
        }

        try {
            // Intentar hacer ping al nodo
            NodeInterface nodeInterface = (NodeInterface) Naming.lookup(node.getRmiUrl());
            boolean available = nodeInterface.ping();

            if (available) {
                updateNodeHeartbeat(node.getId());
            } else {
                // Marcar como offline si no responde
                markNodeAsOffline(node.getId());
            }

            return available;

        } catch (Exception e) {
            logger.warning("El nodo " + node.getIp() + ":" + node.getPort() + " no está disponible: " + e.getMessage());
            markNodeAsOffline(node.getId());
            return false;
        }
    }

    // Obtener estadísticas de todos los nodos
    public String getNodesStatistics() {
        List<Node> allNodes = nodeDAO.getAllNodes();
        long totalNodes = allNodes.size();
        long onlineNodes = allNodes.stream().filter(Node::isOnline).count();
        long offlineNodes = allNodes.stream().filter(Node::isOffline).count();
        long maintenanceNodes = allNodes.stream().filter(Node::isInMaintenance).count();

        return String.format(
                "Estadísticas de Nodos:\n" +
                        "Total de Nodos: %d\n" +
                        "Nodos Online: %d\n" +
                        "Nodos Offline: %d\n" +
                        "Nodos en Mantenimiento: %d",
                totalNodes, onlineNodes, offlineNodes, maintenanceNodes);
    }

    // Inicializar nodos por defecto del sistema
    public void initializeDefaultNodes() {
        cleanupInvalidNodes();

        // Definir nodos por defecto
        String[][] defaultNodes = {
                { "127.0.0.1", "1100" },
                { "127.0.0.1", "1101" },
                { "127.0.0.1", "1102" }
        };

        for (String[] nodeInfo : defaultNodes) {
            try {
                String ip = nodeInfo[0];
                int port = Integer.parseInt(nodeInfo[1]);

                if (!nodeDAO.nodeExists(ip, port)) {
                    registerNode(ip, port);
                    logger.info("Nodo por defecto inicializado: " + ip + ":" + port);
                } else {
                }
            } catch (Exception e) {
                logger.warning(
                        "Error al inicializar nodo por defecto " + nodeInfo[0] + ":" + nodeInfo[1] + ": " + e.getMessage());
            }
        }
    }

    // Iniciar monitoreo de salud de nodos
    private void startNodeHealthMonitoring() {
        scheduler.scheduleAtFixedRate(this::checkNodesHealth, 1, 2, TimeUnit.MINUTES);
    }

    // Verificar la salud de todos los nodos
    private void checkNodesHealth() {
        List<Node> onlineNodes = getOnlineNodes();
        LocalDateTime now = LocalDateTime.now();

        for (Node node : onlineNodes) {
            try {
                if (node.getLastHeartbeat() != null) {
                    long minutesSinceLastHeartbeat = ChronoUnit.MINUTES.between(node.getLastHeartbeat(), now);

                    if (minutesSinceLastHeartbeat > HEARTBEAT_TIMEOUT_MINUTES) {
                        logger.warning("El nodo " + node.getIp() + ":" + node.getPort() + " no ha enviado latido por "
                                + minutesSinceLastHeartbeat + " minutos");

                        // Intentar hacer ping
                        if (!isNodeAvailable(node)) {
                            logger.warning("Marcando nodo " + node.getIp() + ":" + node.getPort() + " como OFFLINE");
                            markNodeAsOffline(node.getId());
                        }
                    }
                }

            } catch (Exception e) {
                logger.severe(
                        "Error al verificar la salud del nodo " + node.getIp() + ":" + node.getPort() + ": " + e.getMessage());
            }
        }
    }

    // Obtener nodo por ID
    public Node getNodeById(Long nodeId) {
        return nodeDAO.getNodeById(nodeId);
    }

    // Limpiar nodos inválidos (que no son de almacenamiento)
    private void cleanupInvalidNodes() {
        try {
            List<Node> allNodes = getAllNodes();

            for (Node node : allNodes) {
                // Si el nodo no está en el rango de puertos de almacenamiento (1100-1199)
                // o está en puertos de servicios (8080, 8081, 8082, 1099), marcarlo como
                // offline
                if (node.getPort() < 1100 || node.getPort() > 1199 ||
                        node.getPort() == 8080 || node.getPort() == 8081 ||
                        node.getPort() == 8082 || node.getPort() == 1099) {

                    logger.info("Marcando nodo inválido como offline: " + node.getIp() + ":" + node.getPort() +
                            " (no es un nodo de almacenamiento)");
                    markNodeAsOffline(node.getId());
                }
            }

        } catch (Exception e) {
            logger.warning("Error durante la limpieza de nodos inválidos: " + e.getMessage());
        }
    }

    // Cerrar el manager
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Cierre de NodeManager completado");
    }
}
