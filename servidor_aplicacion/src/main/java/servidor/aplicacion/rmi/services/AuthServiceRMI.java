package servidor.aplicacion.rmi.services;

import servidor.aplicacion.dto.LoginResponse;
import servidor.aplicacion.dto.UserDTO;
import servidor.aplicacion.rmi.interfaces.AuthInterfaceRMI;
import servidor.aplicacion.services.AuthService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class AuthServiceRMI extends UnicastRemoteObject implements AuthInterfaceRMI {

    private final AuthService authService;

    public AuthServiceRMI() throws RemoteException {
        super();
        this.authService = new AuthService();
    }

    @Override
    public LoginResponse login(String username, String password) throws RemoteException {
        try {
            // Usar el método login principal
            servidor.aplicacion.dto.LoginRequest request = new servidor.aplicacion.dto.LoginRequest(username, password);
            return authService.login(request);
        } catch (Exception e) {
            throw new RemoteException("Error during login: " + e.getMessage(), e);
        }
    }

    @Override
    public UserDTO getUserById(Long userId) throws RemoteException {
        try {
            return authService.getUserById(userId);
        } catch (Exception e) {
            throw new RemoteException("Error getting user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UserDTO> getAllUsers() throws RemoteException {
        try {
            return authService.getAllUsers();
        } catch (Exception e) {
            throw new RemoteException("Error getting all users: " + e.getMessage(), e);
        }
    }

    @Override
    public UserDTO createUser(String username, String email, String password) throws RemoteException {
        try {
            return authService.createUser(username, email, password);
        } catch (Exception e) {
            throw new RemoteException("Error creating user: " + e.getMessage(), e);
        }
    }

    @Override
    public String ping() throws RemoteException {
        return "Auth RMI Service is alive - " + System.currentTimeMillis();
    }
}