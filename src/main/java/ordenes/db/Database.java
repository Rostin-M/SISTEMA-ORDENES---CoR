package ordenes.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Punto único de acceso a la conexión JDBC. Los datos de conexión se
 * pueden sobrescribir con las variables de entorno DB_URL, DB_USER y
 * DB_PASSWORD; si no existen, se usan los valores por defecto que
 * coinciden con el script db/schema.sql y el usuario que este mismo
 * proyecto crea en MySQL Workbench.
 */
public final class Database {

    private static final String URL_POR_DEFECTO = "jdbc:mysql://localhost:3306/sistema_ordenes";
    private static final String USUARIO_POR_DEFECTO = "app_ordenes";
    private static final String PASSWORD_POR_DEFECTO = "AppOrdenes2026!";

    private Database() {
    }

    public static Connection obtenerConexion() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL", URL_POR_DEFECTO);
        String usuario = System.getenv().getOrDefault("DB_USER", USUARIO_POR_DEFECTO);
        String password = System.getenv().getOrDefault("DB_PASSWORD", PASSWORD_POR_DEFECTO);
        return DriverManager.getConnection(url, usuario, password);
    }
}
