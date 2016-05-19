package org.kaariboga.agents;

import java.lang.InterruptedException;
import java.util.Enumeration;

import org.kaariboga.core.*;
import org.kaariboga.plugins.domainPlugIn.*;

/**
 * Utility agent that prints out a list of all servers connected to the domain.
 * Note how easy it is to extend an existing program with agents.
 * Future versions of kaariboga will probably contain special agents that
 * are automatically integrated into the menu structure.
 */
public class ServerLister extends Kaariboga
{

    /**
     *  The object this class wants to access at a base.
     */
    private transient DomainService serviceObject;

    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public ServerLister(String name){
        super("ServerLister_" + name);
    }

    /**
     * Prints out the names of all servers connected to the domain.
     */
    public void run(){
        KaaribogaAddress address;

	Object service = base.getServiceObject( this, "kaariboga.org/DomainService" );
        if (   ( service != null )
            && ( service instanceof DomainService ) )
        {
            serviceObject = (DomainService) service;
	    Enumeration enumServers = serviceObject.getServers().elements();

	    System.out.println("---------------------------------------------");
            System.out.println("Servers connected to the domain:");
            while ( enumServers.hasMoreElements() ){
                address = (KaaribogaAddress) enumServers.nextElement();
                System.out.println( address );
            }
            System.out.println("---------------------------------------------");
	}
        fireDestroyRequest();
    }

}
