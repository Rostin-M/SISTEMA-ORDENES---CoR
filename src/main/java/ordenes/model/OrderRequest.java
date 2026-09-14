package ordenes.model;

/**
 * Representa una solicitud entrante del cliente para crear una orden.
 * Viaja a través de toda la cadena de verificaciones; cada eslabón la
 * lee y, en algunos casos, la completa con información nueva (por
 * ejemplo, AuthenticationHandler anota si el usuario es administrador).
 */
public class OrderRequest {

    private final String nombreUsuario;
    private final String password;
    private final String direccionIp;
    private String datosOrden;
    private final boolean accionAdministrativa;

    private boolean autenticado = false;
    private boolean esAdministrador = false;

    public OrderRequest(String nombreUsuario, String password, String direccionIp,
                         String datosOrden, boolean accionAdministrativa) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.direccionIp = direccionIp;
        this.datosOrden = datosOrden;
        this.accionAdministrativa = accionAdministrativa;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPassword() {
        return password;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public String getDatosOrden() {
        return datosOrden;
    }

    public void setDatosOrden(String datosOrden) {
        this.datosOrden = datosOrden;
    }

    public boolean isAccionAdministrativa() {
        return accionAdministrativa;
    }

    public boolean isAutenticado() {
        return autenticado;
    }

    public void setAutenticado(boolean autenticado) {
        this.autenticado = autenticado;
    }

    public boolean isEsAdministrador() {
        return esAdministrador;
    }

    public void setEsAdministrador(boolean esAdministrador) {
        this.esAdministrador = esAdministrador;
    }

    /**
     * Clave usada por CacheHandler para identificar solicitudes
     * repetidas: mismo usuario pidiendo los mismos datos.
     */
    public String claveDeCache() {
        return nombreUsuario + "|" + datosOrden;
    }
}
