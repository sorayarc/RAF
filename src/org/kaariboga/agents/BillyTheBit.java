package org.kaariboga.agents;

import java.io.Serializable;
import java.lang.InterruptedException;
import java.lang.Math;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;

import org.kaariboga.core.*;
import org.kaariboga.plugins.domainPlugIn.*;

/**
 * Travels around in a domain at random and lets a message
 * pop up on every computer at random choices.
 * The domain server must be running for this agent to work.
 * After a number of maxTravels it kills itself.
 *
 * @author Toby Ornotoby
 */
public class BillyTheBit extends Kaariboga
{
    String[] messages =
        {"Don't waste your time on searching bugs! Implement new features!",
         "Every bug is worth an update!",
         "Make your money with updates!",
         "Don't waste your money on quality! Spend it on marketing!",
         "If the customer gets the program he wants, he won't by a new one tomorrow!",
         "Make your program big! The customer will think it's powerful!",
         "Waste computation time! State of the art programs need state of the art computers!",
         "Fullfill your customer's expectation: No pane, no game!",
         "Customer's expectation: High price, high quality!",
         "Every bug gives the user the chance to blame someone else for the errors!",
         "Some say the universe was build out of chaos.\nGood recipe to build programs!",
         "Good programmers may ruin your company! No chance for updates!",
         "If your rival is better today, say you will be better tomorrow!",
         "Don't worry about bugs! The customers will find them!",
         "The good thing about marketing is, that you can convince your customers about just anything!",
         "Say the program is useful! Some will believe you!",
         "Sell insecure programs today, sell firewalls tomorrow!",
         "Make it big! People love big things.",
         "Support? Ah, you are talking of the money line.",
         "If you can't make it good, make it look good!"};

    /**
     * List of all the servers in the domain.
     */
    Vector servers;

    /**
     * The message that shows up.
     */
    String message;	

    /**
     * Maximum count of travels.
     */
    int maxTravels;

    /**
     * Count of travels.
     */
    int cTravels; 


    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public BillyTheBit(String name){
        super("BillyTheBit_" + name);
    }

    /**
     * Initializes v with all servers connected to the domain.
     */
    public void onCreate(){
        servers = new Vector();

	Object service = base.getServiceObject( this, "kaariboga.org/DomainService" );
        if (   ( service != null )
            && ( service instanceof DomainService ) )
        {
            DomainService serviceObject = (DomainService) service;
	    Enumeration enumServers = serviceObject.getServers().elements();
            while ( enumServers.hasMoreElements() ){
                servers.addElement (enumServers.nextElement());
            }
            maxTravels = 10 * servers.size();
        }
    }

    /**
     * This is automically called if the agent arrives on
     * a base.
     */
    public void run(){
        try{
            if (cTravels < maxTravels){		
                ++cTravels;
	        int i = (int)Math.floor (servers.size() * Math.random());	
                destination = (KaaribogaAddress) servers.elementAt(i);
	        int j = (int)Math.floor (messages.length * Math.random());			
                message = messages[j];
                new Popup().start();		
                Thread.sleep(10000);
                fireDispatchRequest();
            }
            else fireDestroyRequest();
        }
        catch (ArrayIndexOutOfBoundsException e){
            System.err.println ("BillyTheBit: Index out of bounds!");
            fireDestroyRequest();
        }
	catch (InterruptedException e){
            System.err.println ("BillyTheBit: Sleep interrupted!");
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
            message = "Billy the Bit says: \n" + message;
            dialog.showMessageDialog (null, message);
        }
    }

}
