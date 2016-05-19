package org.kaariboga.core;

/**
 * This is the class for messages that can be fired by kaaribogas.
 *
 * @author Dirk Struve
 */
public class KaaribogaMessageEvent extends java.util.EventObject
{
    /**
     * The message encapsuleted by this event.
     */
    private KaaribogaMessage m;

    /**
     * @param obj The object that sends this message.
     * @param m The message encapsuleted by this event.
     */
    public KaaribogaMessageEvent(Object obj, KaaribogaMessage m){
        super(obj);
        this.m = m;
    }

    /**
     * Returns the message encapsulated by this event.
     */
    public KaaribogaMessage getMessage(){
        return m;
    }
}