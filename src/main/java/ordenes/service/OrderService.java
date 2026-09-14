package ordenes.service;

import ordenes.model.OrderRequest;
import ordenes.model.OrderResponse;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representa, de forma simplificada, al sistema real de órdenes en
 * línea. Solo se ejecuta cuando una solicitud logró pasar por
 * toda la cadena de verificaciones, así que aquí ya no se repite ninguna
 * de esas reglas: este servicio confía en que, si fue invocado, la solicitud es válida.
 */
public final class OrderService {

    private static final AtomicInteger CONSECUTIVO = new AtomicInteger(1000);

    private OrderService() {
    }

    public static OrderResponse crearOrden(OrderRequest request) {
        int numeroOrden = CONSECUTIVO.incrementAndGet();
        String datosRespuesta = "orden #" + numeroOrden + " (" + request.getDatosOrden() + ")";
        return OrderResponse.exito("Orden creada correctamente.", datosRespuesta);
    }
}
