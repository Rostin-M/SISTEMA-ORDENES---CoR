package ordenes.chain;

import ordenes.model.OrderRequest;
import ordenes.model.OrderResponse;

/**
 * Tercer eslabón: sanea los datos crudos de la solicitud antes de que
 * lleguen más adentro del sistema. Aquí se aplica una regla simple a
 * modo de ejemplo (rechazar datos vacíos o con contenido que sugiera
 * un intento de inyección) y se limpian espacios sobrantes.
 */
public class SanitizationHandler extends BaseHandler {

    private static final String[] PATRONES_PROHIBIDOS = {"--", ";", "<script", "drop table"};

    @Override
    protected OrderResponse check(OrderRequest request) {
        String datos = request.getDatosOrden();

        if (datos == null || datos.isBlank()) {
            return OrderResponse.error("Datos de la orden vacíos: no hay nada que procesar.");
        }

        String datosEnMinuscula = datos.toLowerCase();
        for (String patron : PATRONES_PROHIBIDOS) {
            if (datosEnMinuscula.contains(patron)) {
                return OrderResponse.error(
                        "Datos de la orden rechazados: contienen contenido no permitido.");
            }
        }

        request.setDatosOrden(datos.trim());
        return null;
    }
}
