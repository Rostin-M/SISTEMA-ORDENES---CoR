package ordenes.chain;

import ordenes.db.Database;
import ordenes.model.OrderRequest;
import ordenes.model.OrderResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Primer eslabón de la cadena: confirma que las credenciales de la
 * solicitud correspondan a un usuario real. Si fallan, no tiene
 * sentido evaluar el resto de las verificaciones, así que la cadena
 * se detiene aquí mismo. También deja registrado cada intento, exitoso
 * o no, para que RateLimitHandler pueda usar ese historial más
 * adelante en la cadena.
 */
public class AuthenticationHandler extends BaseHandler {

    @Override
    protected OrderResponse check(OrderRequest request) {
        try (Connection conexion = Database.obtenerConexion()) {
            String sql = "SELECT es_administrador FROM usuarios "
                    + "WHERE nombre_usuario = ? AND password_hash = SHA2(?, 256)";
            try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setString(1, request.getNombreUsuario());
                statement.setString(2, request.getPassword());
                try (ResultSet resultado = statement.executeQuery()) {
                    boolean autenticado = resultado.next();
                    registrarIntento(conexion, request, autenticado);

                    if (!autenticado) {
                        return OrderResponse.error(
                                "Autenticación fallida: usuario o contraseña incorrectos.");
                    }
                    request.setAutenticado(true);
                    request.setEsAdministrador(resultado.getBoolean("es_administrador"));
                    return null;
                }
            }
        } catch (SQLException e) {
            return OrderResponse.error("No fue posible validar las credenciales: " + e.getMessage());
        }
    }

    private void registrarIntento(Connection conexion, OrderRequest request, boolean exitoso)
            throws SQLException {
        String sql = "INSERT INTO intentos_login (direccion_ip, nombre_usuario, exitoso) VALUES (?, ?, ?)";
        try (PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, request.getDireccionIp());
            statement.setString(2, request.getNombreUsuario());
            statement.setBoolean(3, exitoso);
            statement.executeUpdate();
        }
    }
}
