package org.kaariboga.core;


/**
 * Interface for events that can be fired by the kaariboga base.
 *
 * @author Dirk Struve
 */
public interface BaseListener extends java.util.EventListener{

    /**
     * Reaction when a kaariboga agent has been put to a base and
     * the base invoked the agent's onCreate method.
     */
    public void baseKaaribogaCreated (BaseEvent e);

    /**
     * Reaction when a kaariboga agent has arrived on the base or
     * has been added on creation.
     */
    public void baseKaaribogaArrived (BaseEvent e);

    /**
     * Reaction when a kaariboga agent left the base.
     */
    public void baseKaaribogaLeft (BaseEvent e);

    /**
     * Reaction when a kaariboga agent has been destroyed.
     */
    public void baseKaaribogaDestroyed (BaseEvent e);


}