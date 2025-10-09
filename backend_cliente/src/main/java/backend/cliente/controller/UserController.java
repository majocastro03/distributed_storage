package backend.cliente.controller;

import backend.cliente.soap.SoapClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import servidor.aplicacion.dto.LoginResponseSOAP;
import servidor.aplicacion.dto.UserDTO;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final SoapClients soapClients;

    public UserController(@Value("${soap.auth.wsdl}") String authWsdl,
                          @Value("${soap.file.wsdl}") String fileWsdl) {
        this.soapClients = new SoapClients(authWsdl, fileWsdl);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseSOAP> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password) throws Exception {
        var auth = soapClients.createAuthPort();
        LoginResponseSOAP resp = auth.login(username, password);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable("id") Long id) throws Exception {
        var auth = soapClients.createAuthPort();
        UserDTO user = auth.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> listUsers() throws Exception {
        var auth = soapClients.createAuthPort();
        List<UserDTO> users = auth.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password) throws Exception {
        var auth = soapClients.createAuthPort();
        UserDTO created = auth.createUser(username, email, password);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() throws Exception {
        var auth = soapClients.createAuthPort();
        String r = auth.ping();
        return ResponseEntity.ok(r);
    }
}