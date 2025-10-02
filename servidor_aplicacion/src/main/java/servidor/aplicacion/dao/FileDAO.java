package servidor.aplicacion.dao;

import servidor.aplicacion.config.DatabaseConnection;
import servidor.aplicacion.model.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Data Access Object para operaciones de archivos en base de datos
public class FileDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public File save(File file) throws SQLException {
        String sql = "INSERT INTO files (name, parent_id, owner_id, type, size) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, file.getName());
            pstmt.setObject(2, file.getParentId());
            pstmt.setLong(3, file.getOwnerId());
            pstmt.setInt(4, file.getType());
            pstmt.setLong(5, file.getSize());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creación de archivo fallida, no se afectaron filas.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    file.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creación de archivo fallida, no se obtuvo ID.");
                }
            }
        }

        return file;
    }

    public File findById(long id) throws SQLException {
        String sql = "SELECT * FROM files WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFile(rs);
                }
            }
        }

        return null;
    }

    public List<File> findByParentIdAndUser(long parentId, long userId) {
        String sql = "SELECT * FROM files WHERE parent_id = ? AND owner_id = ?";
        List<File> files = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, parentId);
            pstmt.setLong(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    files.add(mapResultSetToFile(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return files;
    }

    public List<File> findByOwnerId(long ownerId) {
        String sql = "SELECT * FROM files WHERE owner_id = ?";
        List<File> files = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, ownerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    files.add(mapResultSetToFile(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return files;
    }

    public File update(File file) throws SQLException {
        String sql = "UPDATE files SET name = ?, parent_id = ?, owner_id = ?, type = ?, size = ? WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, file.getName());
            pstmt.setObject(2, file.getParentId());
            pstmt.setLong(3, file.getOwnerId());
            pstmt.setInt(4, file.getType());
            pstmt.setLong(5, file.getSize());
            pstmt.setLong(6, file.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Actualización de archivo fallida, no se afectaron filas.");
            }
        }

        return file;
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM files WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Eliminación de archivo fallida, no se afectaron filas.");
            }
        }
    }

    private File mapResultSetToFile(ResultSet rs) throws SQLException {
        File file = new File();
        file.setId(rs.getLong("id"));
        file.setName(rs.getString("name"));
        file.setParentId(rs.getObject("parent_id", Long.class));
        file.setOwnerId(rs.getLong("owner_id"));
        file.setType(rs.getInt("type"));
        file.setSize(rs.getLong("size"));
        file.setCreatedAt(rs.getTimestamp("created_at"));
        file.setUpdatedAt(rs.getTimestamp("updated_at"));
        return file;
    }
}
