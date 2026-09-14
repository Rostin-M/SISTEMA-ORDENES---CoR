package ordenes.chain;

import ordenes.model.OrderRequest;
import ordenes.model.OrderResponse;

/**
 * Segundo eslabón: una vez se sabe quién es el usuario, confirma que
 * tenga el permiso necesario para la acción solicitada. Las acciones
 * administrativas solo las puede completar un usuario marcado como
 * administrador; crear una orden normal no requiere ese permiso.
 */
public class PermissionHandler extends BaseHandler {

    @Override
    protected OrderResponse check(OrderRequest request) {
        if (request.isAccionAdministrativa() && !request.isEsAdministrador()) {
            return OrderResponse.error(
                    "Permisos insuficientes: esta acción requiere un usuario administrador.");
        }
        return null;
    }
}
