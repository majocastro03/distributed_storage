package servidor.aplicacion.dto;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "loginResponse")
@XmlType(name = "LoginResponseType", namespace = "http://dto.aplicacion.servidor/")
public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private UserDTO user;
    private String sessionToken;
    
    // Constructores
    public LoginResponse() {}
    
    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public LoginResponse(boolean success, String message, UserDTO user, String sessionToken) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.sessionToken = sessionToken;
    }
    
    // Getters y Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
    
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    
    @Override
    public String toString() {
        return "LoginResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", user=" + user +
                ", sessionToken='" + sessionToken + '\'' +
                '}';
    }
}