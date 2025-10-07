package servidor.aplicacion.soap.interfaces;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import servidor.aplicacion.dto.LoginResponseSOAP;
import servidor.aplicacion.dto.UserDTO;
import java.util.List;

@WebService(name = "AuthInterfaceSOAP", targetNamespace = "http://soap.aplicacion.servidor/")
public interface AuthInterfaceSOAP {

    @WebMethod(operationName = "login")
    @WebResult(name = "loginFullResponse")
    LoginResponseSOAP login(@WebParam(name = "username") String username,
            @WebParam(name = "password") String password);

    @WebMethod
    UserDTO getUserById(@WebParam(name = "userId") Long userId);

    @WebMethod
    List<UserDTO> getAllUsers();

    @WebMethod
    UserDTO createUser(@WebParam(name = "username") String username,
            @WebParam(name = "email") String email,
            @WebParam(name = "password") String password);

    @WebMethod
    String ping();
}