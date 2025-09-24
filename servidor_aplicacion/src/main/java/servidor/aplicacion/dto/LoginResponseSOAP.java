package servidor.aplicacion.dto;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "loginResponseSOAP")
@XmlType(name = "LoginResponseSOAPType", namespace = "http://dto.aplicacion.servidor/")
public class LoginResponseSOAP implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private UserDTO user;
    private String sessionToken;
    
    // Constructores
    public LoginResponseSOAP() {}
    
    public LoginResponseSOAP(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public LoginResponseSOAP(boolean success, String message, UserDTO user, String sessionToken) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.sessionToken = sessionToken;
    }
    
    // Constructor para convertir desde LoginResponse
    public LoginResponseSOAP(LoginResponse loginResponse) {
        this.success = loginResponse.isSuccess();
        this.message = loginResponse.getMessage();
        this.user = loginResponse.getUser();
        this.sessionToken = loginResponse.getSessionToken();
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
        return "LoginResponseSOAP{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", user=" + user +
                ", sessionToken='" + sessionToken + '\'' +
                '}';
    }
}