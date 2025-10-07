package servidor.aplicacion.rmi.interfaces;

import servidor.aplicacion.dto.LoginResponse;
import servidor.aplicacion.dto.UserDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AuthInterfaceRMI extends Remote {

        LoginResponse login(String username, String password)
                        throws RemoteException;

        UserDTO getUserById(Long userId)
                        throws RemoteException;

        List<UserDTO> getAllUsers()
                        throws RemoteException;

        UserDTO createUser(String username, String email, String password)
                        throws RemoteException;

        String ping() throws RemoteException;
}