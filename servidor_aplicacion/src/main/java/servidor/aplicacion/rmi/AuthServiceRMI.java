package servidor.aplicacion.rmi;

import servidor.aplicacion.dto.LoginResponse;
import servidor.aplicacion.dto.UserDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AuthServiceRMI extends Remote {

        LoginResponse login(String username, String password)
                        throws RemoteException;

        LoginResponse loginSimple(String username, String password)
                        throws RemoteException;

        UserDTO getUserById(Long userId)
                        throws RemoteException;

        List<UserDTO> getAllUsers()
                        throws RemoteException;

        UserDTO createUser(String username, String email, String password)
                        throws RemoteException;

        String ping() throws RemoteException;
}