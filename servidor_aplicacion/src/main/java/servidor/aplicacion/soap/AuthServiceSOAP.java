package servidor.aplicacion.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import servidor.aplicacion.dto.LoginResponseSOAP;
import servidor.aplicacion.dto.UserDTO;
import java.util.List;

@WebService(name = "AuthService", targetNamespace = "http://soap.aplicacion.servidor/")
public interface AuthServiceSOAP {
    
    @WebMethod(operationName = "loginFull")
    @WebResult(name = "loginFullResponse")
    LoginResponseSOAP login(@WebParam(name = "username") String username,
                           @WebParam(name = "password") String password);
    
    @WebMethod(operationName = "loginSimple")
    @WebResult(name = "loginSimpleResponse")
    LoginResponseSOAP loginSimple(@WebParam(name = "username") String username,
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