package org.kaariboga.io;

import java.util.LinkedList;

import org.kaariboga.core.*;
import org.kaariboga.util.Log;


/**
 *  The DeliveryService receives messages and dispatches them to other bases.
 *  At the moment messages are delivered using TCP/IP socket connections, but it
 *  future other connections or protocols may be supported. For example a
 *  DeliveryService might use HTTP to tunnel through a firewall.
 */
public class DeliveryService
implements KbMessageHandler
{
   
    /**
     *  This user is contacted in case something goes wrong with a posted message.
     */
    protected KbMessageHandler user;

    /**
     *  Port on which this service receives messages
     */
    protected int port = 10102;

    /**
     *  List of all threads currently sending messages
     */
    protected LinkedList sendingThreads;

    /**
     *  List of all threads currently receiving messages
     */
    protected LinkedList receivingThreads;

    /**
     *  Thread listening for incoming messages
     */
    ListenThread listenThread;


    /**
     *  Creates a new delivery service.
     *  To go online call goOnline() first.
     *
     *  @param user A user of this service, who receives error messages.
     *  @param port The network port, this receiver should listen on.
     */
    public DeliveryService( KbMessageHandler user, int port ){
        Log.write( Log.INFORMATION, "DeliveryService: Starting service. Port: " + port );
        this.user = user;
        this.port = port;
        sendingThreads = new LinkedList();
        receivingThreads = new LinkedList();
    }


    /**
     *  Delivers this message to the destination.
     *  Note, that there is no method parameter for a DeliveryServiceUser.
     *  On errors allways the global DeliveryServiceUser is notified.
     *  This is becaus agents should never post messages directy. They
     *  should use the underlying base.
     *  This method has no return parameter for success of the operation,
     *  because messages are handled asynchronously and it can't be determined
     *  at call time, if the deliveery succeeds. On error the DeliveryServiceUser
     *  is notified.
     *
     *  @param message The message, which should be transfered. The message
     *                 allready contains sender and recipient.
     */
    public void postMessage( KaaribogaMessage message ){
        new SendMessageThread( message, user, sendingThreads ).start();
    }

    /**
     *  Called by a receiving thread when a message has arrived.
     *
     *  @param message The message, that has arrived.
     */
    public void handleMessage( KaaribogaMessage message ){
        user.handleMessage( message );
    }


    /**
     *  Called in case a message could not be delivered.
     *  It is not guaranteed, that this method is called in every case, for example
     *  if a DeliveryService is offline, it may decide to store incoming messages
     *  until it is online again.
     *
     *  @param messageId Id of the message, that could not be delivered.
     *  @param errorCode Delivery error code. At the moment the following codes exit:
     *                   BASE_NOT_REACHABLE, AGENT_NOT_REACHABLE, BASE_OFFLINE
     */
    public void messageDeliveryError( long messageId, int errorCode ){
        // XXX do something
    }


    /**
     *  Connects to the internet.
     */
    public void goOnline(){
        Log.write( Log.DEBUG, "DeliveryService.goOnline" ); 
        listenThread = new ListenThread( this, port, receivingThreads );
        listenThread.start();
    }


    /**
     *  Removes connection to the internet.
     */
    public void goOffline(){
        Log.write( Log.DEBUG, "DeliveryService.goOffline" ); 
        if ( listenThread != null ) listenThread.terminate();
        listenThread = null;
    }


} 











