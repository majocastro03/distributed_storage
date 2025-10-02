package servidor.aplicacion.soap;

import jakarta.jws.WebService;
import servidor.aplicacion.dto.LoginRequest;
import servidor.aplicacion.dto.LoginResponse;
import servidor.aplicacion.dto.LoginResponseSOAP;
import servidor.aplicacion.dto.UserDTO;
import servidor.aplicacion.service.AuthService;
import servidor.aplicacion.service.NodeIntegratedService;
import servidor.aplicacion.App;
import java.util.List;
import java.util.logging.Logger;

@WebService(endpointInterface = "servidor.aplicacion.soap.AuthServiceSOAP")
public class AuthServiceSOAPImpl implements AuthServiceSOAP {

    private static final Logger logger = Logger.getLogger(AuthServiceSOAPImpl.class.getName());
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
        // Método de login alternativo para datos de prueba
        LoginResponse response = authService.loginWithSimpleHash(username, password);
        return new LoginResponseSOAP(response);
    }

    @Override
    public UserDTO getUserById(Long userId) {
        return authService.getUserById(userId);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        logger.info("Solicitando lista de usuarios via SOAP");
        try {
            // Intentar obtener usuarios a través del sistema de nodos
            NodeIntegratedService nodeService = App.getNodeService();
            if (nodeService != null) {
                logger.info("ATTEMPTING: Getting users through node system");
                List<UserDTO> result = nodeService.getUsersFromNode();
                logger.info("RESPONSE: Successfully returned " + result.size() + " users via node system");
                return result;
            } else {
                logger.warning("FALLBACK: Node service not available, using direct service");
                List<UserDTO> result = authService.getAllUsers();
                logger.info("RESPONSE: Returned " + result.size() + " users via direct service");
                return result;
            }
        } catch (Exception e) {
            logger.warning("ERROR: Getting users through nodes failed, falling back to direct service: " + e.getMessage());
            List<UserDTO> result = authService.getAllUsers();
            logger.info("FALLBACK RESPONSE: Returned " + result.size() + " users via direct service");
            return result;
        }
    }

    @Override
    public UserDTO createUser(String username, String email, String password) {
        return authService.createUser(username, email, password);
    }

    @Override
    public String ping() {
        try {
            // Incluir información básica de nodos en el ping
            NodeIntegratedService nodeService = App.getNodeService();
            if (nodeService != null) {
                List<servidor.aplicacion.model.Node> onlineNodes = nodeService.getOnlineNodes();
                return "Auth Service OK - " + onlineNodes.size() + " nodos responden";
            } else {
                return "Auth Service OK - Sistema nodos no disponible";
            }
        } catch (Exception e) {
            return "Auth Service OK - Error: " + e.getMessage();
        }
    }
}