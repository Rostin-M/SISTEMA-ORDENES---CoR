package ordenes.chain;

import ordenes.db.Database;
import ordenes.model.OrderRequest;
import ordenes.model.OrderResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Primer eslabón de la cadena: protege contra fuerza bruta contando
 * cuántos intentos fallidos ha habido en los últimos minutos desde la
 * misma dirección IP, sin importar a qué usuario correspondan. Va
 * antes que AuthenticationHandler a propósito: si fuera después,
 * nunca se alcanzaría mientras las credenciales sigan siendo
 * incorrectas, porque la autenticación fallida ya detendría la
 * cadena antes de llegar aquí. Si se supera el umbral, la cadena se
 * detiene aunque las credenciales de esta solicitud en particular
 * sean correctas.
 */
public class RateLimitHandler extends BaseHandler {

    private static final int MAXIMO_INTENTOS_FALLIDOS = 5;
    private static final int VENTANA_EN_MINUTOS = 15;

    @Override
    protected OrderResponse check(OrderRequest request) {
        String sql = "SELECT COUNT(*) AS total FROM intentos_login "
                + "WHERE direccion_ip = ? AND exitoso = FALSE "
                + "AND intentado_en > (NOW() - INTERVAL ? MINUTE)";

        try (Connection conexion = Database.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, request.getDireccionIp());
            statement.setInt(2, VENTANA_EN_MINUTOS);
            try (ResultSet resultado = statement.executeQuery()) {
                resultado.next();
                int intentosFallidos = resultado.getInt("total");
                if (intentosFallidos >= MAXIMO_INTENTOS_FALLIDOS) {
                    return OrderResponse.error(
                            "Demasiados intentos fallidos desde " + request.getDireccionIp()
                                    + " en los últimos " + VENTANA_EN_MINUTOS
                                    + " minutos. Intenta más tarde.");
                }
                return null;
            }
        } catch (SQLException e) {
            return OrderResponse.error("No fue posible validar el límite de intentos: " + e.getMessage());
        }
    }
}
