package org.kaariboga.agents;

import java.lang.InterruptedException;

import org.kaariboga.core.Kaariboga;
import org.kaariboga.core.KaaribogaEvent;

/**
 * The HelloAgents just prints Hello World to the screen.
 */
public class HelloAgent extends Kaariboga
{
    /**
     * How often did the agent travel?
     */
    int trips = 0;

    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public HelloAgent(String name){
        super("Hallodri_" + name);
    }

    /**
     * This is automically called if the agent arrives on
     * a base.
     */
    public void run(){
        System.out.println("Hello World! ");
        if (trips > 0) fireDestroyRequest();
    }

    /**
     * Called by the base when the agent arrives on the base.
     */
    public void onArrival(){
        ++trips;
    }
}