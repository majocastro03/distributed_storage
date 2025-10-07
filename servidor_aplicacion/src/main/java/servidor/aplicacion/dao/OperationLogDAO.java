package servidor.aplicacion.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import servidor.aplicacion.config.DatabaseConnection;
import servidor.aplicacion.model.OperationLog;

public class OperationLogDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // Guarda un registro de operación en la base de datos
    public void save(OperationLog log) throws SQLException {
        String sql = "INSERT INTO operation_logs (user_id, file_id, action, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, log.getUserId());
            if (log.getFileId() != null) {
                stmt.setLong(2, log.getFileId());
            } else {
                stmt.setNull(2, java.sql.Types.BIGINT);
            }
            stmt.setString(3, log.getAction());
            stmt.setInt(4, log.getStatus());
            stmt.executeUpdate();
        }
    }
}
