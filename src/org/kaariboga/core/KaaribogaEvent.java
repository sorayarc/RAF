package org.kaariboga.core;

/**
 * This is the class for all events that can be fired by kaaribogas.
 * Normaly this events are handled in an asynchronous way.
 *
 * @author Dirk Struve
 */
public class KaaribogaEvent extends java.util.EventObject
{
    /**
     * Send by an agent if it wants to be transfered to a new location.
     */ 
    public static final int DISPATCH_REQUEST = 1;
    
    /**
     * Send by an agent if it wants to be put asleep.
     */ 
    public static final int SLEEP_REQUEST = 2;
    
    /**
     * Send by an agent if it wants to be destroyed.
     */ 
    public static final int DESTROY_REQUEST = 3;
    
    /**
     * The events id. This can be DISPATCH_REQUEST,
     * SLEEP_REQUEST or DESTROY_REQUEST.
     */
    protected int id;
    
    /**
     * Creates a new kaariboga event.
     *
     * @param obj The object that created the event.
     * @param id The events id. This can be DISPATCH_REQUEST,
     * SLEEP_REQUEST or DESTROY_REQUEST.
     */
    public KaaribogaEvent(Object obj, int id){
        super(obj); 
        this.id = id;
    } 
    
    /**
     * Returns the id of this message: DISPATCH_REQUEST,
     * SLEEP_REQUEST or DESTROY_REQUEST
     */ 
    public int getID(){
        return id;
    }    
}    