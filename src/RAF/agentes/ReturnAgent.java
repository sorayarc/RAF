package org.kaariboga.agents;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.kaariboga.core.Kaariboga;
import org.kaariboga.core.KaaribogaAddress;
import org.kaariboga.core.KaaribogaEvent;

/**
 * Example of a simple kaariboga that travels to another base
 * returns and destroys itself.
 */
public class ReturnAgent extends Kaariboga
{
    /**
     * Number of trips.
     */
    int trips = 0;

    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public ReturnAgent(String name){
        super("ReturnAgent_" + name);
    }

    /**
     * This method is called when base has done some
     * initialisation on the agent and before run.
     */
    public void onCreate(){
        destination = base.getBaseAddress(this);
    }

    /**
     * Run is called every time an agent get's loaded or
     * arrives on a base.
     */
    public void run(){
        System.out.println("ReturnAgent: I am running! ");
        if (trips == 1){
            System.out.println ("ReturnAgent: Here I am.");
            fireDispatchRequest();
        }
        if (trips > 1){
            System.out.println ("ReturnAgent: Request for destruction.");
            fireDestroyRequest();
        }
    }

    /**
     * This is called before run when the agent arrives from a trip.
     */
    public void onArrival(){
        System.out.println("ReturnAgent: I have arrived from my journey.");
        ++trips;
    }

    /**
     * This method is called before the agent gets dispatched to another base.
     * The method should be used to do clean up when the agent has
     * locked some resources.
     */
    public void onDispatch(){
        System.out.println("ReturnAgent: Sorry, I have to leave you.");
    }
}