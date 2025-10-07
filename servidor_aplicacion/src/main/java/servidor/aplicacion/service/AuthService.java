package servidor.aplicacion.service;

import servidor.aplicacion.dao.UserDAO;
import servidor.aplicacion.dto.LoginRequest;
import servidor.aplicacion.dto.LoginResponse;
import servidor.aplicacion.dto.UserDTO;
import servidor.aplicacion.model.User;
import servidor.aplicacion.util.UserConverter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // Verificar datos de entrada del usuario
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                return new LoginResponse(false, "Username is required");
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return new LoginResponse(false, "Password is required");
            }

            // Buscar usuario
            User user = userDAO.findByUsername(loginRequest.getUsername().trim());

            if (user == null) {
                return new LoginResponse(false, "Invalid username or password");
            }

            // Verificar contraseña
            String hashedInputPassword = hashPassword(loginRequest.getPassword());

            if (!user.getPasswordHash().equals(hashedInputPassword)) {
                return new LoginResponse(false, "Invalid username or password");
            }

            // Generar token de sesión
            String sessionToken = generateSessionToken();

            // Guardar la sesión en la base de datos
            servidor.aplicacion.dao.SessionDAO sessionDAO = new servidor.aplicacion.dao.SessionDAO();
            java.sql.Timestamp createdAt = new java.sql.Timestamp(System.currentTimeMillis());
            // Por ejemplo, expira en 24 horas
            java.sql.Timestamp expiresAt = new java.sql.Timestamp(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
            servidor.aplicacion.model.Session session = new servidor.aplicacion.model.Session(0, user.getId(), sessionToken, createdAt, expiresAt);
            sessionDAO.saveSession(session);

            // Convertir a DTO (sin password)
            UserDTO userDTO = UserConverter.toDTO(user);

            return new LoginResponse(true, "Login successful", userDTO, sessionToken);

        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResponse(false, "Internal server error: " + e.getMessage());
        }
    }

    public UserDTO getUserById(Long id) {
        try {
            User user = userDAO.findById(id);
            return UserConverter.toDTO(user);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<UserDTO> getAllUsers() {
        try {
            List<User> users = userDAO.findAll();
            return UserConverter.toDTOList(users);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UserDTO createUser(String username, String email, String password) {
        try {
            // Verificar si el usuario ya existe
            User existingUser = userDAO.findByUsername(username);
            if (existingUser != null) {
                return null; // Usuario ya existe
            }

            // Crear nuevo usuario
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(hashPassword(password));

            User savedUser = userDAO.create(user);
            return UserConverter.toDTO(savedUser);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private String generateSessionToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
}