package org.kaariboga.io;

import java.io.*;
import java.net.*;
import java.util.*;

import org.kaariboga.core.*;
import org.kaariboga.util.Log;


/**
 * Receives a message through a socket connection.
 */
class ReceiveMessageThread
extends Thread
{
    /**
     *  Socket on which to receive message
     */
    private Socket socket;

    /**
     *  Threads, that are currently receiving messages.
     *  This class adds and removes itself to the collection, because it knows best when to do it.
     */
    private Collection receivingThreads;

    /**
     *  Message handler, that should handle the received message.
     */
    private KbMessageHandler messageHandler;

    /**
     *  Creates a new Thread to receive a message.
     *
     *  @param socket Socket of the incoming connection.
     *  @param handler KbMessageHander to handle the received message
     *  @param receivingThreads List of currently active threads receiving messages.
     */
    public ReceiveMessageThread( Socket socket, KbMessageHandler handler, Collection receivingThreads ){
        this.socket = socket;
	this.messageHandler = handler;
	this.receivingThreads = receivingThreads;
        synchronized( receivingThreads ){
            receivingThreads.add( this );
        }
    }

    /**
     *  Reads message.from a socket and dispatches it to the message handler.
     */
    public void run() {
        ObjectOutputStream outStream = null;
        ObjectInputStream inStream = null;
	KaaribogaMessage message = null;
        InetAddress sender = null;

	try{
            inStream = new ObjectInputStream( new BufferedInputStream( socket.getInputStream() ) );
            sender = socket.getInetAddress();
            message = (KaaribogaMessage) inStream.readObject();
	    Log.write( Log.DEBUG, "ReceiveMessageThread.run: Message has arrived");

            // determine the sender's address
            // This is neccessary, because a server may not know his own address
            // due to dynamic IP addressing.            
            if ( message.kind.equals("GET_MY_IP_ADDRESS") ){
                message.sender.host = socket.getInetAddress();
            }
            
            messageHandler.handleMessage( message );
        }
        catch( IOException e ){
            Log.write( Log.ERROR, "ReceiveMessageThread.run: IOException at data transfer! " + e );
        }
        catch( ClassNotFoundException e ){
	    Log.write( Log.ERROR, "ReceiveMessageThread.run: ClassNotFoundException at receiving the object! " + e );
        }

	// clean up
        try{
            if (inStream != null) inStream.close();
            if (socket != null) socket.close();
        }
        catch (IOException e){
            // Should never happen
            Log.write( Log.ERROR, "ReceiveMessageThread.run: IOException at clean up! " + e );
        }
        synchronized( receivingThreads ){
            receivingThreads.remove( this );
        }
    }


} // ReceiveMessageThread




