package servidor.aplicacion.service;

import servidor.aplicacion.dao.UserDAO;
import servidor.aplicacion.dto.UserDTO;
import servidor.aplicacion.manager.NodeManager;
import servidor.aplicacion.model.Node;
import servidor.aplicacion.interfaces.NodeInterface;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NodeIntegratedService {
    private static final Logger logger = Logger.getLogger(NodeIntegratedService.class.getName());
    private final NodeManager nodeManager;
    private final UserDAO userDAO;
    
    public NodeIntegratedService() {
        this.nodeManager = new NodeManager();
        this.userDAO = new UserDAO();
    }
    
    // Obtener usuarios delegando operación a nodos de almacenamiento
    public List<UserDTO> getUsersFromNode() {
        try {
            // Seleccionar nodo disponible para la operación
            Node selectedNode = nodeManager.selectNodeForOperation();
            
            if (selectedNode == null) {
                logger.warning("NO NODES AVAILABLE - falling back to direct DB access");
                return getUsersDirectly();
            }
            
            logger.info("Delegando obtencion de usuarios al nodo: " + selectedNode.getIp() + ":" + selectedNode.getPort());
            
            try {
                // Establecer conexión RMI con el nodo seleccionado
                String rmiUrl = selectedNode.getRmiUrl();
                logger.info("Connecting to RMI URL: " + rmiUrl);
                
                NodeInterface nodeInterface = (NodeInterface) Naming.lookup(rmiUrl);
                
                // Verificar disponibilidad y respuesta del nodo
                logger.info("Verificando conectividad del nodo con ping");
                boolean pingResult = nodeInterface.ping();
                
                if (!pingResult) {
                    logger.warning("SELECTED NODE NOT RESPONDING to ping - trying another approach");
                    nodeManager.markNodeAsOffline(selectedNode.getId());
                    return getUsersDirectly();
                }
                
                logger.info("NODE RESPONDED to ping successfully: " + selectedNode.getIp() + ":" + selectedNode.getPort());
                
                // Actualizar registro de actividad del nodo
                nodeManager.updateNodeHeartbeat(selectedNode.getId());
                
                // Por ahora, el nodo no maneja usuarios directamente, así que fallback a DB
                // Pero registramos que el NODO SÍ RESPONDIÓ
                logger.info("NODE IS AVAILABLE AND RESPONDING! However, user operations not yet implemented in nodes.");
                logger.info("Using direct DB access as designed, but NODE DID RESPOND to the request");
                return getUsersDirectly();
                
            } catch (Exception e) {
                logger.warning("ERROR communicating with node " + selectedNode.getIp() + ":" + selectedNode.getPort() + " - " + e.getMessage());
                logger.warning("Full error details: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                nodeManager.markNodeAsOffline(selectedNode.getId());
                logger.info("FALLING BACK to direct database access");
                return getUsersDirectly();
            }
            
        } catch (Exception e) {
            logger.severe("SYSTEM ERROR in getUsersFromNode: " + e.getMessage());
            return getUsersDirectly();
        }
    }
    
    // Acceso directo a base de datos cuando nodos no están disponibles
    private List<UserDTO> getUsersDirectly() {
        try {
            logger.info("Direct database query for users");
            List<servidor.aplicacion.model.User> users = userDAO.findAll();
            List<UserDTO> userDTOs = new ArrayList<>();
            
            for (servidor.aplicacion.model.User user : users) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setUsername(user.getUsername());
                userDTO.setEmail(user.getEmail());
                userDTO.setCreatedAt(user.getCreatedAt());
                userDTOs.add(userDTO);
            }
            
            logger.info("SUCCESS: Retrieved " + userDTOs.size() + " users from database");
            return userDTOs;
        } catch (Exception e) {
            logger.severe("ERROR getting users directly from database: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve users", e);
        }
    }
    
    // Registrar nodos de almacenamiento en el sistema
    public void registerSystemNodes() {
        logger.info("Registering system nodes...");
        nodeManager.initializeDefaultNodes();
    }
    
    // Obtener estadísticas de estado y rendimiento de nodos
    public String getNodeStatistics() {
        return nodeManager.getNodesStatistics();
    }
    
    // Obtener lista de nodos activos y disponibles
    public List<Node> getOnlineNodes() {
        return nodeManager.getOnlineNodes();
    }
    
    // Verificar estado de salud y conectividad de todos los nodos
    public void checkNodeHealth() {
        List<Node> onlineNodes = nodeManager.getOnlineNodes();
        logger.info("Checking health of " + onlineNodes.size() + " online nodes");
        
        for (Node node : onlineNodes) {
            boolean available = nodeManager.isNodeAvailable(node);
            logger.info("Node " + node.getIp() + ":" + node.getPort() + " is " + (available ? "available" : "not available"));
        }
    }
    
    // Shutdown del servicio
    public void shutdown() {
        if (nodeManager != null) {
            nodeManager.shutdown();
        }
    }
}