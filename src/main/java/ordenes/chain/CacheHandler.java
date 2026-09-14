package ordenes.chain;

import ordenes.db.Database;
import ordenes.model.OrderRequest;
import ordenes.model.OrderResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Último eslabón de la cadena: revisa si ya existe una respuesta
 * guardada para una solicitud con los mismos datos. Si la hay, no
 * tiene sentido dejar que la solicitud siga hasta el sistema real de
 * órdenes, así que este eslabón resuelve la solicitud por su cuenta y
 * detiene la cadena devolviendo directamente el resultado cacheado.
 * Si no hay nada guardado, deja pasar la solicitud para que el
 * sistema real la procese.
 */
public class CacheHandler extends BaseHandler {

    @Override
    protected OrderResponse check(OrderRequest request) {
        String sql = "SELECT respuesta_json FROM respuestas_cacheadas "
                + "WHERE clave_solicitud = ? AND expira_en > NOW()";

        try (Connection conexion = Database.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, request.claveDeCache());
            try (ResultSet resultado = statement.executeQuery()) {
                if (resultado.next()) {
                    return OrderResponse.exitoDesdeCache(
                            "Respuesta obtenida de caché, no fue necesario reprocesar la orden.",
                            resultado.getString("respuesta_json"));
                }
                return null;
            }
        } catch (SQLException e) {
            return OrderResponse.error("No fue posible consultar el caché: " + e.getMessage());
        }
    }

    /**
     * Guarda en caché el resultado de una orden que sí se procesó,
     * para que la próxima solicitud igual se resuelva sin volver a
     * pasar por el sistema real. La llama OrderClient después de que
     * el sistema real procesa la orden, no la cadena en sí misma.
     */
    public static void guardarEnCache(OrderRequest request, OrderResponse respuesta) {
        if (!respuesta.isExitosa()) {
            return;
        }
        String sql = "INSERT INTO respuestas_cacheadas (clave_solicitud, respuesta_json, expira_en) "
                + "VALUES (?, ?, NOW() + INTERVAL 10 MINUTE) "
                + "ON DUPLICATE KEY UPDATE respuesta_json = VALUES(respuesta_json), "
                + "expira_en = VALUES(expira_en)";

        try (Connection conexion = Database.obtenerConexion();
             PreparedStatement statement = conexion.prepareStatement(sql)) {
            statement.setString(1, request.claveDeCache());
            statement.setString(2, respuesta.getDatos());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("No fue posible guardar en caché: " + e.getMessage());
        }
    }
}
