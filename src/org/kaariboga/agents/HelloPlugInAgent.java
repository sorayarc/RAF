package org.kaariboga.agents;

import java.lang.InterruptedException;

import org.kaariboga.core.Kaariboga;
import org.kaariboga.core.KaaribogaEvent;
import org.kaariboga.plugins.helloPlugIn.*;


/**
 *  The HelloPlugInAgents is an example of how to use a plug-in.
 */
public class HelloPlugInAgent extends Kaariboga
{
    /**
     * How often did the agent travel?
     */
    private int trips = 0;

    /**
     *  The object this class wants to access at a base.
     */
    private transient HelloService serviceObject;

    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public HelloPlugInAgent( String name ){
        super( "Hallodri_" + name );
    }

    /**
     * This is automically called if the agent arrives on
     * a base.
     */
    public void run(){
        Object service = base.getServiceObject( this, "kaariboga.org/HelloService" );
        if (   ( service != null )
            && ( service instanceof HelloService ) )
        {
            serviceObject = (HelloService) service;
            serviceObject.sayHello();
        }

        if (trips > 0) fireDestroyRequest();
    }

    /**
     * Called by the base when the agent arrives on the base.
     */
    public void onArrival(){
        ++trips;
    }
}



