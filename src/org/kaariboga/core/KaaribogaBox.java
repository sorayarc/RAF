package org.kaariboga.core;

import java.net.InetAddress;
import java.util.Date;

/**
 * Stores relevant information to administrate
 * agents on the base.
 */
public class KaaribogaBox
{
    /**
     * Reference to the agent.
     */
    public Kaariboga kaariboga;
    
    /**
     * The agents thread.
     */
    public Thread thread;

    /**
     * Time when the agent arrived on the base or was loaded.
     */
    public Date timeOfArrival;

    /**
     * Address of the host who sent the agent.
     */
    public InetAddress sendingHost;

    /**
     * Creates a new box.
     *
     * @param kaariboga Reference to the agent.
     * @param thread The agent's thread.
     * @param timeOfArrival Date when the agent arrived or was loaded.
     * @param sendingHost The host that sent the agent.
     */
    public KaaribogaBox(Kaariboga kaariboga, Thread thread,
                      Date timeOfArrival, InetAddress sendingHost){
        this.kaariboga = kaariboga;
        this.thread = thread;
        this.timeOfArrival = timeOfArrival;
        this.sendingHost = sendingHost; 
    }    
}