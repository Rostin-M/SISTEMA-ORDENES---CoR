package ordenes.chain;

import ordenes.model.OrderRequest;
import ordenes.model.OrderResponse;

/**
 * Implementación común a todos los eslabones de la cadena. Aquí vive
 * la única parte de la lógica que se repetiría en cada verificación
 * si no existiera esta clase: recordar quién es el siguiente eslabón
 * y decidir si delegarle la solicitud o detener la cadena.
 *
 * Cada verificación concreta solo tiene que implementar check(): si
 * decide que el proceso no puede continuar, ya sea porque encontró un
 * error o porque ella misma resolvió la solicitud (como hace
 * CacheHandler cuando hay una respuesta guardada), devuelve una
 * OrderResponse. Si decide que la solicitud puede seguir su curso,
 * devuelve null y BaseHandler se encarga de pasarla al siguiente
 * eslabón.
 */
public abstract class BaseHandler implements RequestHandler {

    private RequestHandler next;

    @Override
    public void setNext(RequestHandler next) {
        this.next = next;
    }

    @Override
    public final OrderResponse handle(OrderRequest request) {
        OrderResponse resultadoPropio = check(request);
        if (resultadoPropio != null) {
            return resultadoPropio;
        }
        if (next != null) {
            return next.handle(request);
        }
        return OrderResponse.exito("Todas las verificaciones pasaron.", null);
    }

    /**
     * Regla de negocio propia de cada verificación concreta.
     *
     * @return una OrderResponse si esta verificación decide detener la
     *         cadena (por error o porque resolvió la solicitud), o
     *         null si la solicitud puede continuar hacia el siguiente
     *         eslabón.
     */
    protected abstract OrderResponse check(OrderRequest request);
}
