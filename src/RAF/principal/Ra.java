package raf.principal;

import java.io.Serializable;

/**
 * La clase Ra implementa la clase base de un agente móvil.
 * Es capaz de saltar de host a host y correr alli en su propio 
 * thread. Cada agente necesita un objeto RaAgency que maneje sus
 * peticiones y los envia y los recibe.
 *
 * @author RMN
 */
public class Ra implements Runnable, Serializable
{

    /**
     * Lanza eventos de agente al RaListener.
     * Este thread se encesita para lanzar eventos de un modo asincrono.
     * Los eventos son asincronos debido a que el resultado de un evento
     * puede ser la destrucción de un agente. Tambien algunas reacciones a esos
     * eventos pueden consumir tiempo.
     */
    class FireEventThread extends Thread implements Serializable{

        /**
         * El evento que tiene que ser lanzado.
         */
        private RaEvent event;

        /**
         * @param e El evento que tiene que ser lanzado.
         */
        public FireEventThread (RaEvent e){
            event = e;
        }

        /**
         * Chequea los id de las llamadas al método apropiado del
         * listener de eventos.
         */
        public void run(){
            switch (event.id){
                case RaEvent.DISPATCH_REQUEST:
                    raListener.raDispatchRequest(event);
                    break;
                case RaEvent.DESTROY_REQUEST:
                    raListener.raDestroyRequest(event);
                    break;
                case RaEvent.SLEEP_REQUEST:
                    raListener.raSleepRequest(event);
                    break;
            }
        }
    } // FireEventThread

    /**
     * Lanza RaMessage al RaMessageListener.
     * Este thread se necesita para lanzar mensajes de un modo asincrono.
     * Los mensajes se deben lanzar de modo asincrono debido a que un resultado del lanzamiento
     * de mensajes podria ser continuar la comunicación entre
     * objetos. 
     */
    class FireMessageThread extends Thread implements Serializable{

        /**
         * El evento que va a ser lanzado.
         */
        private RaMessageEvent e;

        /**
         * @param m El mensaje que va a ser lanzado.
         */
        public FireMessageThread (RaMessageEvent e){
            this.e = e;
        }

        /**
         * Llama al método RaMessage(m) del RaMesssageListener.
         */
        public void run(){
            messageListener.raMessage(e);
        }
    } // FireMessageThread

    /**
     * La agencia es la RaAgency que aloja al ra. Un ra
     * solamente puede vivir en una agencia.
     */
    protected transient RaAgency agency = null;

    /**
     * Este es el nombre del agente.
     */
    private String name = null;

    /**
     * Este es el destino al que el agente quiere ser transferido.
     * Es leido por la agencia a través del método getDestination().
     */
    protected RaAddress destination;

    /**
     * El listener de eventos que recibe todos los RaEvents. Es normalmente puesto
     * puesto por la agencia en llegada.
     */
    protected transient RaListener raListener;

    /**
     * El listener de emnsajes que recibe todos los RaMessages. Es normalmente puesto por la agencia 
     * en llegada.
     */
    protected transient RaMessageListener messageListener;

    /**
     * Construye un nuevo agente con su nombre.
     * El nombre debe ser unico, debdio a que es usado para
     * administrar agentes en el servidor.
     *
     * @param name El nombre del agente. La clase Agencia proporciona un
     *             metodo que genera un nombre único.
     */
    public Ra(String name){
        this.name = name;
    }

    /**
     *
     * Run es el metodo principal del thread de Ra. Esllamado por la agencia
     * si recibe o crea un nuevo objeto agente.
     */
    public void run(){}

    /**
     * Es llamado por la agencia si es creado o recibido un nuevo agente.
     * Un agente solamente puede existir en la agencia.
     *
     * @param b La RaAgency que aloja al agente.
     */
    public final void setAgency(RaAgency b){
        agency = b;
    }

    /**
     * Devuelve el nombre de este agente.
     */
    public String getName(){
        return name;
    }

    /**
     * Esta funcion es llamada en la primera creacion del agente.
     * En este momento el agente esta incializado.
     */
    public void onCreate(){}

    /**
     * Esta es llamada antes de que el agente sea eliminada por la agencia.
     */
    public void onDestroy(){}

    /**
     * Esta es llamada cuando el agente llega a una nueva agencia.
     */
    public void onArrival(){}

    
    public void onDispatch(){}

   
    public void onSleep(){}

   
    public void onAwake(){}

   
    public void handleMessage(RaMessage msg){}

    
    protected void fireDispatchRequest(){
        RaEvent event = new RaEvent(this, RaEvent.DISPATCH_REQUEST);
        new FireEventThread(event).start();
    }

 
    protected void fireDestroyRequest(){
        RaEvent event = new RaEvent(this, RaEvent.DESTROY_REQUEST);
        new FireEventThread(event).start();
    }

   
    protected void fireSleepRequest(){
        RaEvent event = new RaEvent(this, RaEvent.SLEEP_REQUEST);
        new FireEventThread(event).start();
    }

   
    protected void fireRaMessage(RaMessage m){
        RaMessageEvent event = new RaMessageEvent(this, m);
        new FireMessageThread(event).start();
    }

    public RaAddress getDestination (){
        return destination;
    }


   
    public void addRaListener(RaListener l){
        raListener = l;
    }

   
    public void addRaMessageListener(RaMessageListener l){
        messageListener = l;
    }

    
    public String toString(){
        return name;
    }

}