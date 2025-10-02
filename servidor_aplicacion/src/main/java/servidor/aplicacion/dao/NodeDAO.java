package servidor.aplicacion.dao;

import servidor.aplicacion.config.DatabaseConnection;
import servidor.aplicacion.model.Node;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// LOS NODOS YA FUNCIONAN BIEN, NO TOCAR :)
public class NodeDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public Node registerNode(Node node) {
        String sql = "INSERT INTO nodes (ip, port, status, last_heartbeat) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, node.getIp());
            pstmt.setInt(2, node.getPort());
            pstmt.setInt(3, node.getStatus());
            pstmt.setTimestamp(4, Timestamp.valueOf(node.getLastHeartbeat()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creación de nodo fallida, no se afectaron filas.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    node.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creación de nodo fallida, no se obtuvo ID.");
                }
            }

            return node;
        } catch (SQLException e) {
            throw new RuntimeException("Error registrando nodo: " + node.getIp() + ":" + node.getPort(), e);
        }
    }

    public Node getNodeById(Long id) {
        String sql = "SELECT * FROM nodes WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToNode(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error obteniendo nodo por ID: " + id, e);
        }
    }

    public Node getNodeByIpAndPort(String ip, int port) {
        String sql = "SELECT * FROM nodes WHERE ip = ? AND port = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ip);
            pstmt.setInt(2, port);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToNode(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error obteniendo nodo por IP y puerto: " + ip + ":" + port, e);
        }
    }

    public List<Node> getAllNodes() {
        String sql = "SELECT * FROM nodes ORDER BY id";
        List<Node> nodes = new ArrayList<>();

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                nodes.add(mapResultSetToNode(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error obteniendo todos los nodos", e);
        }

        return nodes;
    }

    public List<Node> getOnlineNodes() {
        String sql = "SELECT * FROM nodes WHERE status = ? ORDER BY id";
        List<Node> nodes = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Node.STATUS_ONLINE);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                nodes.add(mapResultSetToNode(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error obteniendo nodos en línea", e);
        }

        return nodes;
    }

    public boolean updateNodeStatus(Long nodeId, int status, LocalDateTime lastHeartbeat) {
        String sql = "UPDATE nodes SET status = ?, last_heartbeat = ? WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, status);
            pstmt.setTimestamp(2, Timestamp.valueOf(lastHeartbeat));
            pstmt.setLong(3, nodeId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando estado del nodo para ID: " + nodeId, e);
        }
    }

    public boolean updateHeartbeat(Long nodeId) {
        String sql = "UPDATE nodes SET last_heartbeat = ? WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(2, nodeId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando heartbeat para nodo ID: " + nodeId, e);
        }
    }

    public boolean deleteNode(Long nodeId) {
        String sql = "DELETE FROM nodes WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, nodeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando nodo con ID: " + nodeId, e);
        }
    }

    public boolean nodeExists(String ip, int port) {
        String sql = "SELECT COUNT(*) FROM nodes WHERE ip = ? AND port = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ip);
            pstmt.setInt(2, port);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Error verificando si el nodo existe: " + ip + ":" + port, e);
        }
    }

    private Node mapResultSetToNode(ResultSet rs) throws SQLException {
        Node node = new Node();
        node.setId(rs.getLong("id"));
        node.setIp(rs.getString("ip"));
        node.setPort(rs.getInt("port"));
        node.setStatus(rs.getInt("status"));

        Timestamp lastHeartbeat = rs.getTimestamp("last_heartbeat");
        if (lastHeartbeat != null) {
            node.setLastHeartbeat(lastHeartbeat.toLocalDateTime());
        }

        return node;
    }
}
