package servidor.aplicacion.dto;

import java.sql.Timestamp;
// NO TOCAR, FALTA PROBAR
public class FileChunkDTO {
    private Integer id;
    private Long fileId;
    private Long nodeId;
    private Integer chunkIndex;
    private String checksum;
    private Boolean replicated;
    private Timestamp createdAt;
    private String nodeInfo;

    public FileChunkDTO() {
    }

    public FileChunkDTO(Integer id, Long fileId, Long nodeId, Integer chunkIndex,
            String checksum, Boolean replicated) {
        this.id = id;
        this.fileId = fileId;
        this.nodeId = nodeId;
        this.chunkIndex = chunkIndex;
        this.checksum = checksum;
        this.replicated = replicated;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Boolean getReplicated() {
        return replicated;
    }

    public void setReplicated(Boolean replicated) {
        this.replicated = replicated;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(String nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public boolean isReplicated() {
        return replicated != null && replicated;
    }

    public Integer getChunkNumber() {
        return chunkIndex;
    }

    public void setChunkNumber(Integer chunkNumber) {
        this.chunkIndex = chunkNumber;
    }

    public String getChunkHash() {
        return checksum;
    }

    public void setChunkHash(String chunkHash) {
        this.checksum = chunkHash;
    }

    public String getUniqueKey() {
        return fileId + "_" + nodeId + "_" + chunkIndex;
    }

    @Override
    public String toString() {
        return "FileChunkDTO{" +
                "id=" + id +
                ", fileId=" + fileId +
                ", nodeId=" + nodeId +
                ", chunkIndex=" + chunkIndex +
                ", checksum='" + checksum + '\'' +
                ", replicated=" + replicated +
                ", nodeInfo='" + nodeInfo + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        FileChunkDTO that = (FileChunkDTO) o;

        if (!fileId.equals(that.fileId))
            return false;
        if (!nodeId.equals(that.nodeId))
            return false;
        return chunkIndex.equals(that.chunkIndex);
    }

    @Override
    public int hashCode() {
        int result = fileId.hashCode();
        result = 31 * result + nodeId.hashCode();
        result = 31 * result + chunkIndex.hashCode();
        return result;
    }
}