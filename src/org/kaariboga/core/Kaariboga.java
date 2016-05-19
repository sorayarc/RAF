package org.kaariboga.core;

import java.io.Serializable;

/**
 * The Kaariboga class implements the base class of a mobile agent.
 * It is able to jump from host to host and runs there in it's own
 * thread. Each kaariboga needs a KaaribogaBase object that handles it's
 * requests and sends and receives it.
 *
 * @author Dirk Struve
 */
public class Kaariboga implements Runnable, Serializable, KbMessageHandler
{

    /**
     * Fires kaariboga events to the kaaribogaListener.
     * This thread is needed to fire events in an asynchronous way.
     * Asynchronous event firering should be done because result of
     * an event could be destruction of the kaariboga. Also some
     * reactions to these events could be time consuming.
     */
    class FireEventThread extends Thread implements Serializable{

        /**
         * The event that has to be fired.
         */
        private KaaribogaEvent event;

        /**
         * @param e The event that has to be fired.
         */
        public FireEventThread (KaaribogaEvent e){
            event = e;
        }

        /**
         * Checks the events id and calls the appropriate method
         * of the event listener.
         */
        public void run(){
            switch (event.id){
                case KaaribogaEvent.DISPATCH_REQUEST:
                    kaaribogaListener.kaaribogaDispatchRequest(event);
                    break;
                case KaaribogaEvent.DESTROY_REQUEST:
                    kaaribogaListener.kaaribogaDestroyRequest(event);
                    break;
                case KaaribogaEvent.SLEEP_REQUEST:
                    kaaribogaListener.kaaribogaSleepRequest(event);
                    break;
            }
        }
    } // FireEventThread

    /**
     * Fires KaaribogaMessage to the kaaribogaMessageListener.
     * This thread is needed to fire messages in an asynchronous way.
     * Asynchronous message firering should be done because result of
     * of message firering could be continuing communication between
     * objects. Asynchronous messages would result in the objects
     * continuing calling each other methods without giving the
     * methods a change to return.
     */
    class FireMessageThread extends Thread implements Serializable{

        /**
         * The event to be send.
         */
        private KaaribogaMessageEvent e;

        /**
         * @param m The message to be send.
         */
        public FireMessageThread (KaaribogaMessageEvent e){
            this.e = e;
        }

        /**
         * Calls kaariboaMesssageListner's kaaribogaMessage(m) method.
         */
        public void run(){
            messageListener.kaaribogaMessage(e);
        }
    } // FireMessageThread

    /**
     * The base is the KaaribogaBase that hosts the kaariboga. A kaariboga
     * can only live on a base.
     */
    protected transient KaaribogaBase base = null;

    /**
     * This is simply the name of the agent.
     */
    private String name = null;

    /**
     * This is the destination the kaariboga wants to be transfered next.
     * It is read by the base through the getDestination() method.
     */
    protected KaaribogaAddress destination;

    /**
     * The event listener that receives all KaaribogaEvents. It is
     * normally set by the base on arrival.
     */
    protected transient KaaribogaListener kaaribogaListener;

    /**
     * The message listener that receives all KaaribogaMessages. It is
     * normally set by the base on arrival.
     */
    protected transient KaaribogaMessageListener messageListener;

    /**
     * Constructs a new agent with it's name.
     * The name must be unique, because it is used to
     * administrate agents on the server.
     *
     * @param name The name of the agent. The Base class provides a
     *             method to generate a unique name.
     */
    public Kaariboga(String name){
        this.name = name;
    }

    /**
     *
     * Run is the main method of the kaariboga thread. It is called by
     * the kaariboga base if it receives or creates
     * a new Kaariboga object.
     */
    public void run(){}

    /**
     * This is called by the base if a new kaariboga is created or received.
     * A kaariboga can only exist on base.
     *
     * @param b The KaaribogaBase that hosts the agent.
     */
    public final void setBase(KaaribogaBase b){
        base = b;
    }

    /**
     * Returns the name of this agent.
     */
    public String getName(){
        return name;
    }

    /**
     * This function is called on the first creation of the kaariboga.
     * At this time the base is already set.
     */
    public void onCreate(){}

    /**
     * This is called before the kaariboga is destroyed by the base.
     */
    public void onDestroy(){}

    /**
     * This is called when the kaariboga arrives on a new base.
     */
    public void onArrival(){}

    /**
     * This is called before the kaariboga is going to be dispatched
     * to a new location.
     */
    public void onDispatch(){}

    /**
     * Called before the kaariboga is send to sleep, maybe because
     * it is going to be saved on a disk or just to save computation time.
     */
    public void onSleep(){}

    /**
     * Called when the kaariboga is waked up again.
     */
    public void onAwake(){}

    /**
     * Handles all kinds of kaariboga messages.
     * Note that messages can contain arbitrary contents.
     * So you can handle your own set of messages for different
     * kinds of agents.
     */
    public void handleMessage(KaaribogaMessage msg){}

    /**
     *  Called in case a message could not be delivered.
     *  It is not guaranteed, that this method is called in every case, for example
     *  if a DeliveryService is offline, it may decide to store incoming messages
     *  until it is online again.
     *
     *  @param messageId Id of the message, that could not be delivered
     *  @param errorCode Delivery error code. At the moment the following codes exit:
     *                   BASE_NOT_REACHABLE, AGENT_NOT_REACHABLE, BASE_OFFLINE
     */
    public void messageDeliveryError( long messageId, int errorCode ){}

    /**
     * Fires a dispatch request to the kaaribogaListener.
     * Before the event is fired the destination must be set.
     * This event is fired if the kaariboga wants to be send
     * to another base.
     */
    protected void fireDispatchRequest(){
        KaaribogaEvent event = new KaaribogaEvent(this, KaaribogaEvent.DISPATCH_REQUEST);
        new FireEventThread(event).start();
    }

    /**
     * Fires a destroy request to the kaaribogaListener.
     * The kaariboga fires a destroy request if it wants to be
     * destroyed, usually when it's work has been done.
     */
    protected void fireDestroyRequest(){
        KaaribogaEvent event = new KaaribogaEvent(this, KaaribogaEvent.DESTROY_REQUEST);
        new FireEventThread(event).start();
    }

    /**
     * Fires a sleep request to the kaaribogaListener.
     * The sleep request is inteded to be send if the kaariboga
     * wants to be out of action for a longer period of time.
     */
    protected void fireSleepRequest(){
        KaaribogaEvent event = new KaaribogaEvent(this, KaaribogaEvent.SLEEP_REQUEST);
        new FireEventThread(event).start();
    }

    /**
     * Fires a message to the kaaribogaMessageListener.
     * A message is fired if the kaariboga agents wants to get
     * into contact with another agent or to retrieve information
     * from servers.
     */
    protected void fireKaaribogaMessage(KaaribogaMessage m){
        KaaribogaMessageEvent event = new KaaribogaMessageEvent(this, m);
        new FireMessageThread(event).start();
    }

    /**
     * Gets the destination the kaariboga is going to be transfered next.
     */
    public KaaribogaAddress getDestination (){
        return destination;
    }


    /**
     * Adds the event listener for kaariboga events.
     * At the moment a kaariboga can only have one event listener.
     *
     * @param l The KaaribogaEventListener that listens to kaariboga events
     * from this agent, usually the base.
     */
    public void addKaaribogaListener(KaaribogaListener l){
        kaaribogaListener = l;
    }

    /**
     * Adds the message listener for kaariboga messages.
     * At the moment a kaariboga can only have one message listener.
     *
     * @param l The KaaribogaMessageListener that listens to kaariboga messages
     * from this agent, usually the base.
     */
    public void addKaaribogaMessageListener(KaaribogaMessageListener l){
        messageListener = l;
    }

    /**
     * Prints out an agents String representation.
     */
    public String toString(){
        return name;
    }

}

