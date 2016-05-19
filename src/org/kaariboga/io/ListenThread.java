package org.kaariboga.io;

import java.io.*;
import java.net.*;
import java.util.*;

import org.kaariboga.core.KbMessageHandler;
import org.kaariboga.util.Log;


/**
 *  Listens on the specified port and spawns new threads to
 *  receive incoming messages.
 */
public class ListenThread
extends Thread
{

    /**
     *  Should this thread continue to live or terminate?
     */
    private volatile boolean shouldLive = true;

    /**
     *  Handles the incoming messages
     */
    private KbMessageHandler messageHandler;

    /**
     *  Collection of all threads currently receiving messages
     */
    private Collection receivingThreads;

    /**
     *  Port on which to listen for new messages.
     */
    private int port;


    /**
     *  @param handler A KbMessageHandler to handle incoming messages
     *  @param port The port number on which the server socket listens for messages.
     *  @param threads Collection of the threads, currently receiving messages.
     */
    public ListenThread( KbMessageHandler handler, int port, Collection threads ){
        this.messageHandler = handler;
        this.port = port;
        this.receivingThreads = threads;
    }


    /**
     *  Listens on a server socket and spawns new threads to receive messages.
     */
    public void run(){
        Socket socket = null;
	ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket( port );
	    serverSocket.setSoTimeout( 100 );
	}
	catch( IOException e ){
            Log.write( Log.ERROR, "ListenThread.run" + e );
            System.exit(1);
	}

        while (shouldLive){
            try {
		socket = serverSocket.accept();
                Log.write( Log.DEBUG, "ListenThread.run: Getting a message");
                new ReceiveMessageThread( socket, messageHandler, receivingThreads ).start();
                yield();
            }
            catch ( InterruptedIOException e ){
	    // for JDK 1.4: catch ( SocketTimeoutException e ){
                // This happens regularly when the socket times out
                // The outer loop checks, if the server should continue
                // accepting connections
            }
	    catch ( IOException e ){
                Log.write( Log.ERROR, "ListenThread.run: " + e );
                // continue listening
            }
        }
    }


    /**
     *  Terminates this thread.
     *  This method returns before the thread has terminated.
     *  If you want to wait until the thread has terminated use
     *  something like this:
     *  listenThread.terminate();
     *  listenThread.join();
     */
    public void terminate(){
        shouldLive = false;
    }
}






