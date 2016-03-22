package raf.principal;


/**
 * Interfaz para eventos que pueden ser lanzados desde la agencia.
 *
 * @author RMN
 */
public interface AgencyListener extends java.util.EventListener{

    /**
     * Reaccion cuando un agente ra ha sido puesto en la agencia y
     * la agncia invoca el metodo onCreate.
     */
    public void agencyRaCreated (AgencyEvent e);

    /**
     * Reaccion cuando ha llegado un agente a la agencia o
     * ha sido añadido en creacion.
     */
    public void agencyRaArrived (AgencyEvent e);

    /**
     * Reaccion cuando un agente deja la agencia.
     */
    public void agencyRaLeft (AgencyEvent e);

    /**
     * Reaccion cuando un agente ha sido eliminado.
     */
    public void agencyRaDestroyed (AgencyEvent e);


}