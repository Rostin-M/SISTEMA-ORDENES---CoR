package ordenes;

import ordenes.chain.AuthenticationHandler;
import ordenes.chain.CacheHandler;
import ordenes.chain.MaxSizeHandler;
import ordenes.chain.OrderClient;
import ordenes.chain.PermissionHandler;
import ordenes.chain.RateLimitHandler;
import ordenes.chain.RequestHandler;
import ordenes.chain.SanitizationHandler;
import ordenes.model.OrderRequest;
import ordenes.model.OrderResponse;

/**
 * Programa de demostración: envía distintas solicitudes al cliente
 * para mostrar cómo se comporta la cadena en cada caso descrito en el
 * análisis del problema.
 */
public class Main {

    public static void main(String[] args) {
        OrderClient cliente = new OrderClient();

        titulo("1. Solicitud válida de un usuario normal");
        probar(cliente, new OrderRequest("rostin", "clave123", "190.10.20.1",
                "2 laptops", false));

        titulo("2. Credenciales incorrectas");
        probar(cliente, new OrderRequest("rostin", "clave-equivocada", "190.10.20.1",
                "2 laptops", false));

        titulo("3. Usuario normal intentando una acción administrativa");
        probar(cliente, new OrderRequest("rostin", "clave123", "190.10.20.1",
                "cancelar toda la bodega", true));

        titulo("4. Usuario administrador sí puede hacer la misma acción");
        probar(cliente, new OrderRequest("admin", "admin123", "190.10.20.2",
                "cancelar toda la bodega", true));

        titulo("5. Datos con contenido no permitido");
        probar(cliente, new OrderRequest("rostin", "clave123", "190.10.20.1",
                "1 laptop; DROP TABLE usuarios;", false));

        titulo("6. Misma solicitud enviada dos veces (la segunda debería salir de caché)");
        OrderRequest solicitudRepetida = new OrderRequest("rostin", "clave123", "190.10.20.1",
                "3 monitores", false);
        probar(cliente, solicitudRepetida);
        probar(cliente, solicitudRepetida);

        titulo("7. Fuerza bruta: varios intentos fallidos seguidos desde la misma IP");
        String ipAtacante = "45.33.10.99";
        for (int i = 1; i <= 6; i++) {
            System.out.println("  Intento " + i + ":");
            probar(cliente, new OrderRequest("rostin", "clave-mala", ipAtacante,
                    "1 laptop", false));
        }

        titulo("8. Extensibilidad: se agrega una verificación nueva SIN tocar las anteriores");
        RequestHandler tamanoMaximo = new MaxSizeHandler(20);
        RequestHandler limiteIntentos = new RateLimitHandler();
        RequestHandler autenticacion = new AuthenticationHandler();
        RequestHandler permisos = new PermissionHandler();
        RequestHandler saneo = new SanitizationHandler();
        RequestHandler cache = new CacheHandler();

        tamanoMaximo.setNext(limiteIntentos);
        limiteIntentos.setNext(autenticacion);
        autenticacion.setNext(permisos);
        permisos.setNext(saneo);
        saneo.setNext(cache);

        OrderClient clienteExtendido = new OrderClient(tamanoMaximo);
        probar(clienteExtendido, new OrderRequest("rostin", "clave123", "190.10.20.5",
                "una orden con una descripcion demasiado larga para pasar", false));
        probar(clienteExtendido, new OrderRequest("rostin", "clave123", "190.10.20.5",
                "orden corta", false));
    }

    private static void probar(OrderClient cliente, OrderRequest request) {
        logIntento(request);
        OrderResponse respuesta = cliente.enviarSolicitud(request);
        System.out.println("  " + respuesta);
    }

    private static void logIntento(OrderRequest request) {
        System.out.println("  Intentando procesar orden: usuario=\"" + request.getNombreUsuario()
                + "\", contraseña=\"" + request.getPassword() + "\", IP=" + request.getDireccionIp()
                + (request.isAccionAdministrativa() ? ", accion administrativa" : ""));
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("== " + texto + " ==");
    }
}
