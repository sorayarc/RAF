package raf.principal;

/**
 * Los eventos que pueden ser lanzados por la agencia ra.
 * Esos eventos son lanzados cuando un agente Ra es añadido o
 * borrado de una agencia.
 *
 * @author RMN
 */
public class AgencyEvent extends java.util.EventObject
{
    /**
     * ombre del agente que ha sido  añadido o borrado de la agencia.
     */
    private String name;

    /**
     * Crea un nuevo evento de agencia.
     *
     * @param obj El objeto que creo el evento.
     * @param name Nombre del agente.
     */
    public AgencyEvent(Object obj, String name){
        super(obj);
        this.name = name;
    }

    /**
     * Devuelve el nombre del agente que ha sido añadido o borrado de la agencia.
     */
    public String getName(){
        return name;
    }
}