package org.kaariboga.io;

import java.io.*;
import java.net.*;
import java.util.Collection;

import org.kaariboga.core.*;
import org.kaariboga.util.Log;


/**
 * Sends a KaaribogaMessage to another base.
 */
class SendMessageThread
extends Thread
{
    /**
     *  message to send
     */
    KaaribogaMessage message;

    /**
     *  Threads, that are currently sending messages.
     *  This class adds and removes itself to the collection, because it knows best when to do it.
     */
    private Collection sendingThreads;

    /**
     *  Message handler for errors in case the sending fails.
     */
    private KbMessageHandler messageHandler;

    /**
     *  Creates a thread that sends a message to another host.
     *  The message must contain the receiver's address!
     *
     *  @param msg The message to send.
     *  @param handler A KbMessageHandler to handle error messages
     *  @param senders Collection of currently sending threads
     */
    public SendMessageThread( KaaribogaMessage msg, KbMessageHandler handler, Collection senders ){
        this.message = msg;
	this.messageHandler = handler;
	this.sendingThreads = senders;
        synchronized( sendingThreads ){
            sendingThreads.add( this );
        }
    }

    /**
     * Send the message through a socket connection.
     */
    public void run(){
        Socket socket = null;
        ObjectOutputStream outStream = null;
        ObjectInputStream inStream = null;

        // send message
        try {
            Log.write( Log.DEBUG, "SendMessageThread: Try to create socket to: " + message.recipient.host + " " + message.recipient.port );
            socket = new Socket( message.recipient.host, message.recipient.port );         
            outStream = new ObjectOutputStream( socket.getOutputStream() );
            outStream.writeObject( message );
            outStream.flush();
            Log.write( Log.DEBUG, "SendMessageThread: Wrote message to socket" );
        }
        catch( IOException e ){
            messageHandler.messageDeliveryError( message.id, KbMessageHandler.BASE_NOT_REACHABLE );
            Log.write( Log.ERROR, "SendMessageThread.run: " + e);
        }
        
        // clean up
        try{
            if (outStream != null) outStream.close();
            if (socket != null) socket.close();
        }
        catch (IOException e){
            // Should never happen
            Log.write( Log.ERROR, "SendMessageThread.run: IOException at clean up! " + e);
        }
        synchronized( sendingThreads ){
            sendingThreads.remove( this );
        }
    }


} // SendMessageThread





