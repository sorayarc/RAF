package org.kaariboga.core;


/**
 * Interface for messages that can be fired by kaaribogas.
 *
 * @author Dirk Struve
 */
public interface KaaribogaMessageListener extends java.util.EventListener{

    /**
     * The kaariboga wants a message to be send.
     * The message is encapsulated in the event.
     */
    public void kaaribogaMessage (KaaribogaMessageEvent e);
}