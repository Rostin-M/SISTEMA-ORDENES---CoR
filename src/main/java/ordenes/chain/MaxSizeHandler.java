package ordenes.chain;

import ordenes.model.OrderRequest;
import ordenes.model.OrderResponse;

/**
 * Verificación para demostrar la extensibilidad del diseño.
 * Rechaza solicitudes cuyos datos superen un tamaño máximo. Ninguna
 * de las clases existentes (BaseHandler, los otros manejadores,
 * OrderClient) tuvo que modificarse para que esta verificación exista
 * y se pueda insertar en la cadena.
 */
public class MaxSizeHandler extends BaseHandler {

    private final int tamanoMaximo;

    public MaxSizeHandler(int tamanoMaximo) {
        this.tamanoMaximo = tamanoMaximo;
    }

    @Override
    protected OrderResponse check(OrderRequest request) {
        String datos = request.getDatosOrden();
        if (datos != null && datos.length() > tamanoMaximo) {
            return OrderResponse.error("Los datos de la orden superan el tamaño máximo permitido ("
                    + tamanoMaximo + " caracteres).");
        }
        return null;
    }
}
