package org.kaariboga.agents;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import javax.swing.*;

import org.kaariboga.core.Kaariboga;
import org.kaariboga.core.KaaribogaAddress;
import org.kaariboga.core.KaaribogaEvent;
import org.kaariboga.core.KaaribogaMessage;

/**
 * The MessageSender agent sends a message to all agents on the base server.
 */
public class MessageSender extends Kaariboga
{
    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public MessageSender (String name){
        super("MessageSender_" + name);
    }

    /**
     * Sends a message to all agents on this base and destroys itself.
     */
    public void run(){

        // open dialog for message
        JOptionPane dialog = new JOptionPane();
        String content = dialog.showInputDialog (null, "Please type in a message.");

        // send Message
        // Note: if you want to send Messages to other servers you will
        // need code like this:
        // KaaribogaAddress baseAdr  = base.getBaseAddress(this);
        // KaaribogaAddress sender  = new KaaribogaAddress (baseAdr.host, baseAdr.port, getName());
        // KaaribogaAddress recipient = new KaaribogaAddress (host, port, name);
        Enumeration names = base.getKaaribogaNames (this);
        while (names.hasMoreElements()){
            // simple constructors for address save for local use
            KaaribogaAddress sender  = new KaaribogaAddress (getName());
            KaaribogaAddress recipient = new KaaribogaAddress ((String) names.nextElement());
            KaaribogaMessage message = new KaaribogaMessage (sender, recipient, "MESSAGE", content, null);
            fireKaaribogaMessage (message);
        }

        fireDestroyRequest();
    }

}