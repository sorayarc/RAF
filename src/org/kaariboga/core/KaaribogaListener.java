package org.kaariboga.core;


/**
 * Interface for events that can be fired by kaaribogas.
 *
 * @author Dirk Struve
 */
public interface KaaribogaListener extends java.util.EventListener{

    /**
     * Handles an agent's dispatch request when it wants to be
     * transported to another base.
     */
    public void kaaribogaDispatchRequest(KaaribogaEvent e);

    /**
     * Not yet implemented!
     * Intended to handle sleep requests that are fired when
     * a kaariboga wants to be inactive for a longer time.
     */
    public void kaaribogaSleepRequest(KaaribogaEvent e);

    /**
     * Handles the kaaribogas destroy request when it wants to be destroyed.
     */
    public void kaaribogaDestroyRequest(KaaribogaEvent e);
}