package org.kaariboga.plugins.domainPlugIn;

import java.io.*;
import java.lang.Thread;
import java.net.*;
import java.util.*;

import org.kaariboga.core.*;
import org.kaariboga.plugin.*;
import org.kaariboga.util.Log;


/**
 *  An example for the implementation of a plug-in.
 */
public class DomainPlugIn
extends KbPlugIn
implements Serializable
{
    /**
     *  A class that provides a set of methods.
     */
    private DomainService service;

    /**
     *  How many milliseconds to wait between sending of messages
     */
    private long interval;

    /**
     *  Address of the domain server
     */
    private KaaribogaAddress domainServer;

    /**
     *  hashtable containing the server names and addresses.
     */
    Hashtable servers = null;

 
    /**
     *  The message types, this plug-in can handle
     */
    private String[] messageTypes;


    /**
     *  Initialize the plug-in with name and a service name.
     *  A service name is an identifier, which can be used by agents
     *  to find a specific plug-in.
     *  The name is just a unique Id.
     *
     *  @param name Unique Id for this instance.
     */
    public DomainPlugIn( String name ){
        super( name );
        String ticks = null;
        serviceNames = new String[]{ "kaariboga.org/DomainService" };
        service = new DomainService( this );
        messageTypes = new String[]{ "BASES" };
	servers = new Hashtable();
    }

    /**
     *  This method returns an object, that can be used by agents or
     *  the server itself.
     *  An agent usually searches for a service name and checks if the
     *  object this method returns is of the type it expects.
     *
     *  @return An object, that implements a set of service specific methods.
     */
    public Object getService( String name ){
        if ( serviceNames[0].equals(name) )
           return service;
        else 
           return null;
    }


    /**
     *  Notifies the domain server every interval milliseconds
     */
    public void run(){
        if ( domainServer != null ){
            KaaribogaMessage msg;
            while (true){
                try {
		    // Update domain server
                    msg = new KaaribogaMessage( base.getBaseAddress(this), domainServer, "BASE_ONLINE", null, null );
                    fireKaaribogaMessage( msg );

                    // Update local server list
                    msg = new KaaribogaMessage( base.getBaseAddress(this), domainServer, "GET_BASES", null, null );
                    fireKaaribogaMessage( msg );

                    Thread.currentThread().sleep( interval );
                }
                catch( InterruptedException e ){
                    // happens, when sleep is interrupted
                }
            }
        }
    }


    /**
     *  Set configuration options for this plug-in.
     *
     *  @param props Properties for this plug-in.
     */
    public void setProperties( Properties props ){
        if ( props == null ) return;

        try {
            String ticks = props.getProperty("interval");
            if ( ticks != null ){
                interval = Long.parseLong( ticks );
            }
            else interval = 180 * 1000;

            int port = 10102;
            String strPort = props.getProperty("domainPort");
            if ( strPort != null ){
                port = Integer.parseInt( strPort );
            }

            String serverName = props.getProperty("domainServer");
            if ( serverName == null ){
                Log.write( Log.ERROR, "DomainPlugIn.setProperties: No domain server specified." );
            }
            else {
                InetAddress host = InetAddress.getByName( serverName );
                domainServer = new KaaribogaAddress( host, port, null );
            }
        }
        catch( NumberFormatException e ){
            domainServer = null;
            Log.write( Log.ERROR, "DomainPlugIn.setProperties() " + e );
        }
        catch( UnknownHostException e ){
            domainServer = null;
            Log.write( Log.ERROR, "DomainPlugIn.setProperties() " + e );
        }
    }    


    /**
     *  Get the message types, this plug-in can handle.
     *  The messages will be registered at a KaaribogaBase.
     *  The base will delegate appropriate messages to the
     *  right plug-in.
     *
     *  @return An array with the names of the messages
     */
    public String[] getMessageTypes(){
        return messageTypes;
    }

    /**
     *  overwrite handleMessage in Kaariboga
     */    
    public void handleMessage( KaaribogaMessage msg ){
        try {
            if ( msg.kind.equals("BASES") ){
                Log.write( Log.INFORMATION, "DomainPlugIn: message BASES arrived.");
                ByteArrayInputStream bis = new ByteArrayInputStream( msg.binary );
                ObjectInputStream ois = new ObjectInputStream( bis );
                synchronized (this){
                    servers = (Hashtable) ois.readObject();
                }

		// print out servers for debug purposes
                if ( Log.getLoglevel() == Log.DEBUG ){
                    int nServers = servers.size();
                    Log.write( Log.DEBUG , "DomainPlugIn: " + nServers + " servers online." );
		    if ( nServers > 0 ){
		        Enumeration enumServers = servers.elements();
		        while ( enumServers.hasMoreElements() ){
                            Log.write( Log.DEBUG , "DomainPlugIn: " + enumServers.nextElement() );
		        }
		    }
                }
            }
        }
        catch( Exception e ){
            Log.write( Log.ERROR, "DomainPlugIn.handleMessage() " + e );
        }
    }

} 











