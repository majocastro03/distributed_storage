package servidor.aplicacion.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import servidor.aplicacion.config.DatabaseConnection;
import servidor.aplicacion.model.Session;

public class SessionDAO {
	public boolean saveSession(Session session) {
		String sql = "INSERT INTO sessions (user_id, token, created_at, expires_at) VALUES (?, ?, ?, ?)";
		try (Connection conn = DatabaseConnection.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setLong(1, session.getUserId());
			stmt.setString(2, session.getToken());
			stmt.setTimestamp(3, session.getCreatedAt());
			stmt.setTimestamp(4, session.getExpiresAt());
			int rows = stmt.executeUpdate();
			return rows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
    
}
