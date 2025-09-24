package servidor.aplicacion.soap;

import jakarta.jws.WebService;
import servidor.aplicacion.dto.LoginRequest;
import servidor.aplicacion.dto.LoginResponse;
import servidor.aplicacion.dto.LoginResponseSOAP;
import servidor.aplicacion.dto.UserDTO;
import servidor.aplicacion.service.AuthService;
import java.util.List;

@WebService(endpointInterface = "servidor.aplicacion.soap.AuthServiceSOAP")
public class AuthServiceSOAPImpl implements AuthServiceSOAP {
    
    private final AuthService authService;
    
    public AuthServiceSOAPImpl() {
        this.authService = new AuthService();
    }
    
    @Override
    public LoginResponseSOAP login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        LoginResponse response = authService.login(request);
        return new LoginResponseSOAP(response);
    }
    
    @Override
    public LoginResponseSOAP loginSimple(String username, String password) {
        // Este método usa el hash simple para tus datos de prueba existentes
        LoginResponse response = authService.loginWithSimpleHash(username, password);
        return new LoginResponseSOAP(response);
    }
    
    @Override
    public UserDTO getUserById(Long userId) {
        return authService.getUserById(userId);
    }
    
    @Override
    public List<UserDTO> getAllUsers() {
        return authService.getAllUsers();
    }
    
    @Override
    public UserDTO createUser(String username, String email, String password) {
        return authService.createUser(username, email, password);
    }
    
    @Override
    public String ping() {
        return "Auth Service is alive - " + System.currentTimeMillis();
    }
}