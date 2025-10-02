package servidor.aplicacion.model;

import java.time.LocalDateTime;

public class Node {
    private Long id;
    private String ip;
    private int port;
    private int status; // 1: ONLINE, 2: OFFLINE, 3: MAINTENANCE
    private LocalDateTime lastHeartbeat;

    public static final int STATUS_ONLINE = 1;
    public static final int STATUS_OFFLINE = 2;
    public static final int STATUS_MAINTENANCE = 3;

    public Node() {
        this.lastHeartbeat = LocalDateTime.now();
        this.status = STATUS_ONLINE;
    }

    public Node(String ip, int port) {
        this();
        this.ip = ip;
        this.port = port;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public boolean isOnline() {
        return this.status == STATUS_ONLINE;
    }

    public boolean isOffline() {
        return this.status == STATUS_OFFLINE;
    }

    public boolean isInMaintenance() {
        return this.status == STATUS_MAINTENANCE;
    }

    public void setOnline() {
        this.status = STATUS_ONLINE;
        updateHeartbeat();
    }

    public void setOffline() {
        this.status = STATUS_OFFLINE;
    }

    public void setMaintenance() {
        this.status = STATUS_MAINTENANCE;
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
    }

    public String getRmiUrl() {
        return "rmi://" + ip + ":" + port + "/StorageNode";
    }

    public String getStatusString() {
        switch (status) {
            case STATUS_ONLINE:
                return "ONLINE";
            case STATUS_OFFLINE:
                return "OFFLINE";
            case STATUS_MAINTENANCE:
                return "MAINTENANCE";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", status=" + getStatusString() +
                ", lastHeartbeat=" + lastHeartbeat +
                '}';
    }
}
