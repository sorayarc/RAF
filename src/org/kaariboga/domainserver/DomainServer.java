package org.kaariboga.domainserver;

import java.io.*;
import java.net.*;
import java.util.*;

import org.kaariboga.core.*;
import org.kaariboga.io.*;
import org.kaariboga.util.*;


/**
 *  A class to build up domains for kaariboga servers.
 *  This class manages a list of all servers connected to the
 *  domain and provides the means for other servers to get this
 *  information.
 *  In addition, an agent server may use this domain server to
 *  determine it's own IP address. Servers who get their IP address
 *  dynamically may have problems of determing their own address,
 *  because in pure Java it is not possible to get the IP address
 *  for a specific device.
 *
 *  Incoming Messages:
 *  BASE_ONLINE  sent by an agent server when the agent server wants
 *               to connect to this domain.
 *  BASE_OFFLINE sent by an agent server when the agent server wants
 *               to disconnect from this domain.
 *  GET_BASES    sent by an agent server to get a list of all servers
 *               connected to this domain.
 *  GET_MY_IP_ADDRESS sent by an agent server, who wants to know it's
 *               own IP address. This is useful for servers who get
 *               their IP address dynamically.
 *
 *  Outgoing Messages:
 *  BASES        sent by this server to nodify an agent server of all
 *               other servers connected.
 *  YOUR_IP_ADDRESS sent by this server as an respond to GET_MY_IP_ADDRESS
 */
public class DomainServer
implements KbMessageHandler,
           SimpleTimerListener
{
    /**
     *  port on which to listen
     */
    int port;
   
    /**
     *  Address of this server
     */
    KaaribogaAddress address;

    /**
     *  Service for message transportation across network
     */
    DeliveryService deliveryService;

    /**
     *  time interval in millis after which old entries are deleted
     */
    long interval;

    /**
     *  timer, that triggers cleaning of old entries.
     */
    SimpleTimer timer;

    /**
     *  Bases connected to this domain plus some extra information
     */
    Hashtable baseEntries;

    /**
     *  Bases connected to this domain
     */
    Hashtable bases;


    /**
     *  @param port port on which to listen
     */
    public DomainServer( int port, long interval ){
        try {
            Log.write( Log.INFORMATION, "DomainServer: Starting domain server. Port: " + port + " timeInterval: " + interval );
            this.port = port;
            this.interval = interval;
            baseEntries = new Hashtable();
            bases = new Hashtable();
            address = new KaaribogaAddress( InetAddress.getLocalHost(), port, null );
            Log.write( Log.INFORMATION, "DomainServer.DomainServer: Local address: " + address.host );
            deliveryService = new DeliveryService( this, port );
            deliveryService.goOnline();
            // XXX removed this, because it shutdown causes a stack overflow, but it should be active
            //Runtime.getRuntime().addShutdownHook( new Shutdown(this) );
            timer = new SimpleTimer( interval, false, this );
            timer.start();
        }
        catch( UnknownHostException e ){
            Log.write( Log.ERROR, "DomainServer.DomainServer: Could not determine address of local host " + e );
            System.exit(1);
        }
    }


    /**
     *  Called when a message for the DeliveryServiceUser has arrived.
     *
     *  @param message The message, that has arrived for the user.
     */
    public void handleMessage( KaaribogaMessage message )
    {
        if ( !message.recipient.host.equals(address.host) ){
            // message relaying
            Log.write( Log.INFORMATION, "DomainServer.handleMessage relaying message from " + message.sender.host + " " + message.sender.port );
            deliveryService.postMessage( message );
        }        
        else if ( message.kind.equals("BASE_ONLINE") ){
            Log.write( Log.INFORMATION, "DomainSerer.handleMessage: Message BASE_ONLINE arrived: " + message.sender.host + " " + message.sender.port );
            addBase( message.sender.toString(), message.sender );
        }
        else if ( message.kind.equals("BASE_OFFLINE") ){
            Log.write( Log.INFORMATION, "DomainSerer.handleMessage: Message BASE_OFFLINE arrived: " + message.sender.host + " " + message.sender.port );
            removeBase( message.sender.toString() );
        }
        else if ( message.kind.equals("GET_BASES") ){
            Log.write( Log.INFORMATION, "DomainSerer.handleMessage: Message GET_BASES arrived: " + message.sender.host + " " + message.sender.port );
            sendBasesList( message.sender );
        }
        else if ( message.kind.equals("GET_MY_IP_ADDRESS") ){
            Log.write( Log.INFORMATION, "DomainSerer.handleMessage: Message GET_MY_IP_ADDRESS arrived: " + message.sender.host + " " + message.sender.port );
            // This is neccessary, because a server may not know his own address
            // due to dynamic IP addressing.
            // The sender field of the message has been modified by the receiving thread to get the sending host right
            KaaribogaAddress receiver = message.sender;
            KaaribogaMessage reply = new KaaribogaMessage( address, receiver, "YOUR_IP_ADDRESS", "", null );
            deliveryService.postMessage( reply );
        }
    }


    /**
     *  Called in case a message could not be delivered.
     *  It is not guaranteed, that this method is called in every case, for example
     *  if a DeliveryService is offline, it may decide to store incoming messages
     *  until it is online again.
     *
     *  @param messageId Id of the message, that could not be delivered
     *  @param errorCode Delivery error code. At the moment the following codes exit:
     *                   BASE_NOT_REACHABLE, AGENT_NOT_REACHABLE, BASE_OFFLINE
     */
    public void messageDeliveryError( long messageId, int errorCode ){
        //XXX
    }


    /**
     *  Removes old base entries from the list of bases connected to the domain.
     *  An entry is an old entry, if it's last update time is older than the given interval.
     *  Not suited for heavy load, but should work well enough for small domains
     */
    public synchronized void onSimpleTimer()
    {
        Log.write( Log.DEBUG, "DomainServer.onSimpleTimer: Removing old base entries." );
        BaseEntry entry;
        long time = System.currentTimeMillis();
        LinkedList oldEntries = new LinkedList();

        // determine old entries
        Enumeration entries = baseEntries.elements();
        while ( entries.hasMoreElements() ){
            entry = (BaseEntry) entries.nextElement();
            if ( time - entry.lastUpdate > interval ) oldEntries.add( entry.hashkey );
        }

        // remove old entries
        Iterator it = oldEntries.iterator();
        while ( it.hasNext() ){
            Object key = it.next();
            bases.remove( key );
            baseEntries.remove( key );
        }
    }


    /**
     *  terminates this server
     */
    public void terminate(){
        Log.write( Log.DEBUG, "DomainServer.terminate" );
        timer.terminate();
        deliveryService.goOffline();
    }


    /**
     *  Adds a new KaaribogaBase to this domain
     *
     *  @param hashcode Hashcode, that should be used to store the base.
     *  @param base KaaribogaBase to add
     */
    protected synchronized void addBase( String hashcode, KaaribogaAddress base )
    {
        long time = System.currentTimeMillis();

        if ( bases.containsKey( hashcode ) ){
            // allready present, just update time
            BaseEntry entry = (BaseEntry) baseEntries.get( hashcode );
            entry.lastUpdate = time;
            ++entry.count;
        }
        else {
            // add new entry
            bases.put( hashcode, base );
            baseEntries.put( hashcode, new BaseEntry( hashcode, base ) );
        }
    }


    /**
     *  Removes a base from this domain.
     *
     *  @param hashcode Hashcode, that should be used to store the base.
     */
    protected synchronized void removeBase( String hashcode ){
        bases.remove( hashcode );
        baseEntries.remove( hashcode );
    }


    /**
     *  Sends a list of all bases connected to the domain to a specified server.
     *
     *  @param receiver the server that receives the list
     */
    public void sendBasesList( KaaribogaAddress receiver )
    {
        try {
            KaaribogaMessage message = null;
            Hashtable servers = null;
            ByteArrayOutputStream bos = null;
            ObjectOutputStream oos = null;
    
            synchronized (this){
                servers = (Hashtable) bases.clone();
            }
	    Log.write( Log.DEBUG, "DomainServer.sendBasesList: " + servers.size() + " bases online." );
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream( bos );
            oos.writeObject( servers );

            message = new KaaribogaMessage( address, receiver, "BASES", "", bos.toByteArray() );
            deliveryService.postMessage( message );
        }
        catch( IOException e ){
            // should not happen, because only the memory operations can through this
            Log.write( Log.ERROR, "DomainServer.sendBasesList: " + e );
        }        
    }


} 
 











