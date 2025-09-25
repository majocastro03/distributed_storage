package backend.cliente;

import servidor.aplicacion.dto.LoginResponse;
import servidor.aplicacion.dto.UserDTO;
import servidor.aplicacion.rmi.AuthServiceRMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class AuthRMITestClient {
    
    public static void main(String[] args) {
        try {
            // Conectar al registro RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            AuthServiceRMI authService = (AuthServiceRMI) registry.lookup("AuthService");
            
            System.out.println("=== Probando Cliente Auth RMI ===");
            
            // Ping al servidor
            System.out.println("Ping: " + authService.ping());
            
            // Listar todos los usuarios
            System.out.println("\n1. Listando todos los usuarios...");
            List<UserDTO> users = authService.getAllUsers();
            users.forEach(user -> System.out.println("  - " + user));
            
            // Probar login con alice
            System.out.println("\n2. Probando login con alice...");
            LoginResponse loginAlice = authService.loginSimple("alice", "cualquier_password");
            System.out.println("Login Alice: " + loginAlice);
            
            // Probar login con bob
            System.out.println("\n3. Probando login con bob...");
            LoginResponse loginBob = authService.loginSimple("bob", "cualquier_password");
            System.out.println("Login Bob: " + loginBob);
            
            // Probar login con admin
            System.out.println("\n4. Probando login con admin...");
            LoginResponse loginAdmin = authService.loginSimple("admin", "cualquier_password");
            System.out.println("Login Admin: " + loginAdmin);
            
            // Probar login con usuario inexistente
            System.out.println("\n5. Probando login con usuario inexistente...");
            LoginResponse loginFail = authService.loginSimple("noexiste", "password");
            System.out.println("Login Failed: " + loginFail);
            
            // Obtener usuario por ID
            System.out.println("\n6. Obteniendo usuario por ID (1)...");
            UserDTO user1 = authService.getUserById(1L);
            System.out.println("Usuario ID 1: " + user1);
            
            System.out.println("\n✅ Todas las pruebas Auth RMI completadas!");
            
        } catch (Exception e) {
            System.err.println("❌ Error en cliente Auth RMI:");
            e.printStackTrace();
        }
    }
}