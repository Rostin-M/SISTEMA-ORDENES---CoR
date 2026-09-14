package ordenes.chain;

import ordenes.model.OrderRequest;
import ordenes.model.OrderResponse;
import ordenes.service.OrderService;

/**
 * Cumple el rol de "Client" del patrón: arma la cadena una sola vez,
 * en el orden que corresponde, y le entrega cada
 * solicitud entrante al primer eslabón. No conoce los detalles de
 * ninguna verificación individual, solo sabe que existe un primer
 * eslabón al que preguntarle.
 */
public class OrderClient {

    private final RequestHandler primerEslabon;

    public OrderClient() {
        RequestHandler limiteIntentos = new RateLimitHandler();
        RequestHandler autenticacion = new AuthenticationHandler();
        RequestHandler permisos = new PermissionHandler();
        RequestHandler saneo = new SanitizationHandler();
        RequestHandler cache = new CacheHandler();

        // limiteIntentos va primero: si una IP ya acumuló demasiados
        // intentos fallidos, no tiene sentido ni siquiera intentar
        // autenticar de nuevo. Si RateLimitHandler fuera después de
        // AuthenticationHandler, nunca se alcanzaría mientras las
        // credenciales sigan siendo incorrectas, porque la
        // autenticación fallida ya detiene la cadena antes.
        limiteIntentos.setNext(autenticacion);
        autenticacion.setNext(permisos);
        permisos.setNext(saneo);
        saneo.setNext(cache);
        // cache es el último eslabón: si nadie más resolvió ni rechazó
        // la solicitud, ella decide si ya existe una respuesta guardada.

        this.primerEslabon = limiteIntentos;
    }

    /**
     * Permite construir la cadena con un primer eslabón distinto,
     * por ejemplo para insertar una verificación nueva antes de las
     * demás sin tocar esta clase. Se usa en la demostración de
     * extensibilidad.
     */
    public OrderClient(RequestHandler primerEslabon) {
        this.primerEslabon = primerEslabon;
    }

    public OrderResponse enviarSolicitud(OrderRequest request) {
        OrderResponse resultadoCadena = primerEslabon.handle(request);

        if (!resultadoCadena.isExitosa()) {
            return resultadoCadena;
        }
        if (resultadoCadena.isDesdeCache()) {
            return resultadoCadena;
        }

        OrderResponse respuestaReal = OrderService.crearOrden(request);
        CacheHandler.guardarEnCache(request, respuestaReal);
        return respuestaReal;
    }
}
