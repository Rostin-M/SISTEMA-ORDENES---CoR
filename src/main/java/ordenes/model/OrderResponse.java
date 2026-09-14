package ordenes.model;

/**
 * Resultado que puede devolver cualquier eslabón de la cadena o el
 * sistema real de órdenes. Un eslabón que encuentra un problema arma
 * una respuesta con exitosa = false; uno que resuelve la solicitud
 * por su cuenta, como CacheHandler cuando hay un dato guardado, arma
 * una respuesta con exitosa = true y desdeCache = true.
 */
public class OrderResponse {

    private final boolean exitosa;
    private final String mensaje;
    private final String datos;
    private final boolean desdeCache;

    private OrderResponse(boolean exitosa, String mensaje, String datos, boolean desdeCache) {
        this.exitosa = exitosa;
        this.mensaje = mensaje;
        this.datos = datos;
        this.desdeCache = desdeCache;
    }

    public static OrderResponse error(String mensaje) {
        return new OrderResponse(false, mensaje, null, false);
    }

    public static OrderResponse exito(String mensaje, String datos) {
        return new OrderResponse(true, mensaje, datos, false);
    }

    public static OrderResponse exitoDesdeCache(String mensaje, String datos) {
        return new OrderResponse(true, mensaje, datos, true);
    }

    public boolean isExitosa() {
        return exitosa;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getDatos() {
        return datos;
    }

    public boolean isDesdeCache() {
        return desdeCache;
    }

    @Override
    public String toString() {
        String estado = exitosa ? (desdeCache ? "EXITO (desde cache)" : "EXITO") : "RECHAZADA";
        return "[" + estado + "] " + mensaje + (datos != null ? " -> " + datos : "");
    }
}
