package ordenes.chain;

import ordenes.model.OrderRequest;
import ordenes.model.OrderResponse;

/**
 * Rol "Handler" del patrón Chain of Responsibility. Toda verificación
 * de la cadena, sin importar cuál sea su regla de negocio, se puede
 * tratar de manera uniforme a través de esta interfaz: se le puede
 * indicar quién es el siguiente eslabón y se le puede entregar una
 * solicitud para que decida qué hacer con ella.
 */
public interface RequestHandler {

    void setNext(RequestHandler next);

    OrderResponse handle(OrderRequest request);
}
