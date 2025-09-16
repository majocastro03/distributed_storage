package servidor.aplicacion;

import java.sql.Connection;

import servidor.aplicacion.config.DatabaseConnection;

/**
 * Hello world!
 *
 */
public class App 
{
public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Conexión exitosa a la base de datos!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
