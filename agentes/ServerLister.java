package raf.agentes;

import java.lang.InterruptedException;
import java.util.Enumeration;

import raf.principal.Ra;
import raf.principal.RaAddress;
import raf.principal.RaEvent;

/**
 * Utility agent that prints out a list of all servers connected to the domain.
 * Note how easy it is to extend an existing program with agents.
 * Future versions of kaariboga will probably contain special agents that
 * are automatically integrated into the menu structure.
 */
public class ServerLister extends Ra
{
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
        RaAddress address;
        Enumeration enum = agency.getServers(this).elements();
        System.out.println("---------------------------------------------");
        System.out.println("Servers connected to the domain:");
        while (enum.hasMoreElements()){
            address = (RaAddress) enum.nextElement();
            System.out.println (address.host.toString() + ":" + Integer.toString(address.port));
        }
        System.out.println("---------------------------------------------");
        fireDestroyRequest();
    }

}
