package org.kaariboga.agents;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.*;

import org.kaariboga.core.Kaariboga;
import org.kaariboga.core.KaaribogaAddress;
import org.kaariboga.core.KaaribogaEvent;
import org.kaariboga.core.KaaribogaMessage;

/**
 * The MessageReceiver agent waits for a message and then prints it out.
 */
public class MessageReceiver extends Kaariboga
{
    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public MessageReceiver (String name){
        super("MessageReceiver_" + name);
    }

    /**
     * Prints kind and content of the received message to the screen.
     *
     * @param msg The message that this agent receives.
     */ 
    public void handleMessage (KaaribogaMessage msg){
        System.out.println ("MessageReceiver: Received Message.");	
        System.out.println ("           Kind: " + msg.kind);	
        System.out.println ("        Content: " + msg.content);	
    }	
     
}