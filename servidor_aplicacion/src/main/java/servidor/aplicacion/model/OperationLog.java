package servidor.aplicacion.model;

import java.sql.Timestamp;

public class OperationLog {
    private Long id;
    private Long userId;
    private Long fileId;
    private String action; // UPLOAD, DOWNLOAD, DELETE, MOVE, REPLICATE, LOGIN
    private int status; // 1: SUCCESS, 2: FAIL
    private Timestamp createdAt;

    public OperationLog() {
    }

    public OperationLog(Long userId, Long fileId, String action, int status) {
        this.userId = userId;
        this.fileId = fileId;
        this.action = action;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", fileId=" + fileId +
                ", action='" + action + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
