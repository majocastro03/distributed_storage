package servidor.aplicacion.model;

import java.sql.Timestamp;

public class File {
    private Long id;
    private String name;
    private Long parentId;
    private Long ownerId;
    private Integer type; // 1: FILE, 2: DIR
    private Long size;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Constructors
    public File() {}
    
    public File(String name, Long parentId, Long ownerId, Integer type, Long size) {
        this.name = name;
        this.parentId = parentId;
        this.ownerId = ownerId;
        this.type = type;
        this.size = size;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public Long getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    
    public Integer getType() {
        return type;
    }
    
    public void setType(Integer type) {
        this.type = type;
    }
    
    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Utility methods for file types
    public static final int FILE_TYPE = 1;
    public static final int DIR_TYPE = 2;
    
    public boolean isFile() {
        return type != null && type == FILE_TYPE;
    }
    
    public boolean isDirectory() {
        return type != null && type == DIR_TYPE;
    }
    
    @Override
    public String toString() {
        return "File{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", ownerId=" + ownerId +
                ", type=" + type +
                ", size=" + size +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
