package servidor.aplicacion.dao;

import servidor.aplicacion.config.DatabaseConnection;
import servidor.aplicacion.model.FileChunk;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
// NO TOCAR AÚN
public class FileChunkDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public FileChunk save(FileChunk chunk) throws SQLException {
        String sql = "INSERT INTO file_chunks (file_id, node_id, chunk_index, checksum, replicated, created_at) " +
                "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, chunk.getFileId());
            pstmt.setLong(2, chunk.getNodeId());
            pstmt.setInt(3, chunk.getChunkIndex());
            pstmt.setString(4, chunk.getChecksum());
            pstmt.setBoolean(5, chunk.isReplicated());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creación de chunk fallida, no se afectaron filas.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    chunk.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creación de chunk fallida, no se obtuvo ID.");
                }
            }
        }

        return chunk;
    }

    public List<FileChunk> findByFileId(long fileId) {
        String sql = "SELECT * FROM file_chunks WHERE file_id = ? ORDER BY chunk_index";
        List<FileChunk> chunks = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, fileId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    chunks.add(mapResultSetToFileChunk(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chunks;
    }

    public List<FileChunk> findByFileIdAndChunkNumber(Long fileId, Integer chunkIndex) {
        String sql = "SELECT * FROM file_chunks WHERE file_id = ? AND chunk_index = ?";
        List<FileChunk> chunks = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, fileId);
            pstmt.setInt(2, chunkIndex);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    chunks.add(mapResultSetToFileChunk(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chunks;
    }

    public List<FileChunk> findByNodeId(long nodeId) {
        String sql = "SELECT * FROM file_chunks WHERE node_id = ?";
        List<FileChunk> chunks = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, nodeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    chunks.add(mapResultSetToFileChunk(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chunks;
    }

    public void deleteByFileId(long fileId) throws SQLException {
        String sql = "DELETE FROM file_chunks WHERE file_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, fileId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Eliminación de chunks fallida, no se afectaron filas.");
            }
        }
    }

    public void delete(long chunkId) throws SQLException {
        String sql = "DELETE FROM file_chunks WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chunkId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Eliminación de chunk fallida, no se afectaron filas.");
            }
        }
    }

    public FileChunk update(FileChunk chunk) throws SQLException {
        String sql = "UPDATE file_chunks SET node_id = ?, checksum = ?, replicated = ? WHERE id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, chunk.getNodeId());
            pstmt.setString(2, chunk.getChecksum());
            pstmt.setBoolean(3, chunk.isReplicated());
            pstmt.setLong(4, chunk.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Actualización de chunk fallida, no se afectaron filas.");
            }
        }

        return chunk;
    }

    public int countByFileId(long fileId) {
        String sql = "SELECT COUNT(*) FROM file_chunks WHERE file_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, fileId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean existsChunk(long fileId, int chunkIndex, long nodeId) {
        String sql = "SELECT COUNT(*) FROM file_chunks WHERE file_id = ? AND chunk_index = ? AND node_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, fileId);
            pstmt.setInt(2, chunkIndex);
            pstmt.setLong(3, nodeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getChunkStatisticsByNode() {
        String sql = "SELECT node_id, COUNT(*) as chunk_count, " +
                "SUM(CASE WHEN replicated = 1 THEN 1 ELSE 0 END) as replicated_count " +
                "FROM file_chunks GROUP BY node_id";

        StringBuilder stats = new StringBuilder("Estadísticas de Chunks por Nodo:\\n");

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                long nodeId = rs.getLong("node_id");
                int chunkCount = rs.getInt("chunk_count");
                int replicatedCount = rs.getInt("replicated_count");

                stats.append(String.format("Nodo %d: %d chunks (%d replicados)\\n",
                        nodeId, chunkCount, replicatedCount));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            stats.append("Error obteniendo estadísticas");
        }

        return stats.toString();
    }

    private FileChunk mapResultSetToFileChunk(ResultSet rs) throws SQLException {
        FileChunk chunk = new FileChunk();
        chunk.setId(rs.getInt("id"));
        chunk.setFileId(rs.getLong("file_id"));
        chunk.setNodeId(rs.getLong("node_id"));
        chunk.setChunkIndex(rs.getInt("chunk_index"));
        chunk.setChecksum(rs.getString("checksum"));
        chunk.setReplicated(rs.getBoolean("replicated"));
        chunk.setCreatedAt(rs.getTimestamp("created_at"));
        return chunk;
    }
}
