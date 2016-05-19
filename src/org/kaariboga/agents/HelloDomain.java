package org.kaariboga.agents;

import java.io.Serializable;
import java.lang.InterruptedException;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;

import org.kaariboga.core.Kaariboga;
import org.kaariboga.core.KaaribogaAddress;
import org.kaariboga.core.KaaribogaEvent;
import org.kaariboga.plugins.domainPlugIn.*;

/**
 * Pops up a hello Window on every server in the domain when
 * a domain server is installed.
 */
public class HelloDomain extends Kaariboga
{
    /**
     * List of all the servers in the domain.
     */
    Vector v;

    /**
     * Points to the next destination in v.
     */
    int i;

    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public HelloDomain(String name){
        super("HelloDomain_" + name);
    }

    /**
     * Initializes v with all servers connected to the domain.
     */
    public void onCreate(){
        Object service = base.getServiceObject( this, "kaariboga.org/DomainService" );
        if (    ( service != null )
             && ( service instanceof DomainService ) )
        {
            DomainService serviceObject = (DomainService) service;
            Enumeration enumServers = serviceObject.getServers().elements();
            i = 0;
            v = new Vector();            
            while ( enumServers.hasMoreElements() ){
                v.addElement( enumServers.nextElement() );
            }
        }
        else fireDestroyRequest();
    }


    /**
     * Shows a window.
     */
    public void onArrival(){
        new Popup().start();
    }

    /**
     * This is automically called if the agent arrives on
     * a base.
     */
    public void run(){
        try{
            if (i < v.size()){
                destination = (KaaribogaAddress) v.elementAt(i);
                ++i;
                System.out.println("Try to dispatch");
                fireDispatchRequest();
            }
            else fireDestroyRequest();
        }
        catch (ArrayIndexOutOfBoundsException e){
            System.err.println ("HelloDomain: Index out of bounds!");
            fireDestroyRequest();
        }
    }

    /**
     * Use a thread to let a window pop up.
     */
    public class Popup extends Thread implements Serializable{

        /**
         * Pop up window.
         */
        public void run(){
            JOptionPane dialog = new JOptionPane();
            dialog.showMessageDialog (null, "Hi there!");
        }
    }

}

