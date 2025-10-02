package servidor.aplicacion.dto;

// VALIDARLO
public class UploadFileRequest {
    private String name;
    private Long parentId;
    private Long ownerId;
    private Integer type;
    private String data;

    public UploadFileRequest() {
    }

    public UploadFileRequest(String name, Long parentId, Long ownerId, Integer type, String data) {
        this.name = name;
        this.parentId = parentId;
        this.ownerId = ownerId;
        this.type = type;
        this.data = data;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "UploadFileRequest{" +
                "name='" + name + '\'' +
                ", parentId=" + parentId +
                ", ownerId=" + ownerId +
                ", type=" + type +
                ", dataLength=" + (data != null ? data.length() : "null") +
                '}';
    }
}