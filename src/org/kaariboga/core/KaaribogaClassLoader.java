package org.kaariboga.core;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.security.cert.Certificate;
import java.security.CodeSource;
import java.security.SecureClassLoader;

import org.kaariboga.io.*;
import org.kaariboga.util.Log;


/**
 * Helper class that creates a class from an array of bytes.
 * This is used in the kaariboga input stream class.
 * When a new agent arrives and the class source code is unknown
 * the KaaribogaClassLoader sends a GET_CLASS message to the host that
 * sent the agent to get the source code.
 *
 */
public class KaaribogaClassLoader
extends SecureClassLoader
implements KbMessageHandler
{

    /**
     * Handles classes and byte codes.
     */
    ClassManager classManager;


    /**
     *  The KaaribogaBase that called this class loader.
     */
    KaaribogaBase base;

    /**
     * The host that holds the source code for the class.
     */
    KaaribogaAddress sourceHost;

    /**
     *  The deliveryService is used to send messages
     */
    DeliveryService deliveryService;

    /**
     *  Message, that contains the answer to a request
     */
    KaaribogaMessage inMessage;

    /**
     *  No incoming message yet
     */
    final int IN_MESSAGE_NONE = 1;
    
    /**
     *  No in message arrived due to some kind of error
     */
    final int IN_MESSAGE_ERROR = 2;
    
    /**
     *  An incoming message has been received.
     */
    final int IN_MESSAGE_ARRIVED = 3;

    /**
     *  Denotes the status of the incoming message.
     *  The incoming message should contain the byte code
     *  of a requested class.
     *  May take the values: IN_MESSAGE_NONE, IN_MESSAGE_ERROR, IN_MESSAGE_ARRIVED
     */
    int statusInMessage;

    /**
     *  Time in millis how long this class loader should wait for a message containing
     *  the bye code of a requested class.
     */
    long timeout;

    /**
     *  Creates a new KaaribogaClassLoader
     *
     *  @param clManager Class manager serving this class loader;
     *                   contains a cache of classes.
     *  @param base The base that uses this class loader; to identify
     *                   the sender in communications.
     *  @param sourceHost The host that serves the byte code for a class.
     *  @param timeout How long to wait for a message containing byte code in millis.
     *  @param deliveryService Is used to send messages across a network
     */
    public KaaribogaClassLoader( ClassManager clManager,
                                 KaaribogaBase base,
                                 KaaribogaAddress sourceHost,
				 long timeout,
                                 DeliveryService deliveryService )
    {
        this.deliveryService = deliveryService;
        this.sourceHost = sourceHost;
        this.base = base;
        this.classManager = clManager;
	this.timeout = timeout;
	statusInMessage = IN_MESSAGE_NONE;
    }


    /**
     *  Called by findClass to get the class byte code.
     *
     *  @param name Name of the class to load.
     *  @return byte array containing the byte code of the requested class or null,
     *          if the byte code could not be loaded.
     */
    protected byte loadClassData( String name )[]
    throws ClassNotFoundException
    {
        byte result[] = null;
        Log.write( Log.INFORMATION, "KaaribogaClassLoader.loadClassData() " + name );
	String id = base.generateName();
        KaaribogaAddress myAddress = new KaaribogaAddress( base.getBaseAddress(this).host,
                                                           base.getBaseAddress(this).port,
	                                                   id );
	base.addMessageHandler( this, id );

	KaaribogaMessage message = new KaaribogaMessage( myAddress,
                                                         sourceHost,
                                                         "GET_CLASS",
                                                          name, null );
        deliveryService.postMessage( message );

	try {
            // wait for incoming message
            synchronized ( this ){
	        wait( timeout );
	    }
	}
	catch( Exception e ){
   	    Log.write( Log.ERROR, "KaaribogaClassLoader.loadClassData() " + e );
	}


        switch ( statusInMessage ){
            case IN_MESSAGE_ARRIVED:
	        // XXX check if inMessage contains the correct class code
	        result = inMessage.binary;
		Log.write( Log.INFORMATION, "KaaribogaClassLoader.loadClassData() got message" );
		break;
	    case IN_MESSAGE_NONE:
		Log.write( Log.INFORMATION, "KaaribogaClassLoader.loadClassData() no message arrived before timeout" );
                break;
	    case IN_MESSAGE_ERROR:
		Log.write( Log.INFORMATION, "KaaribogaClassLoader.loadClassData() no message arrived, because of error" );
		break;
            default:
	        Log.write( Log.ERROR, "KaaribogaClassLoader.loadClassData() unknown value of statusInMessage" );
	}

	base.removeMessageHandler( this, id );
        return result;
    }

    /**
     * Called by the super class method loadClass(String name) to load
     * the class named class.
     */
    public Class findClass(String name)
    throws ClassNotFoundException {
    
        System.out.println("KaaribogaClassLoader.findClass() " + name);
        
	// first look in cache
	Class c = classManager.getClass(name);
        if (c != null) {
            System.out.println("Returning class from cache: " + name);
            classManager.inc(name);
            return c;
        }

	// try to load bytecode from local file system
	byte[] data = classManager.getByteCode (name);
	if( data != null )
		return defineClass( name, data, 0, data.length );
 
	// then load bytecode from another server 
	if( sourceHost != null )
		data = loadClassData(name);
        
        if (data==null) throw new ClassNotFoundException(name);
	try {
            // Java knows no Kaariboga protocol, so we use http as a placeholder
            URL srcURL = new URL ("http", sourceHost.host.getHostAddress(), sourceHost.port, "/");
            CodeSource codeSrc = new CodeSource (srcURL, (Certificate[])null); // to check security
            c = defineClass (name, data, 0, data.length, codeSrc);
        }
	catch (java.net.MalformedURLException e) {
	    Log.write( Log.ERROR, "KaaribogaClassLoader.findClass " + e );
	    throw new ClassNotFoundException(name);
	}

        if (c==null) throw new ClassNotFoundException(name);

        Log.write( Log.INFORMATION, "KaaribogaClassLoader.findClass: Class defined successfully " );
        classManager.addClass(name, c, data);

        return c;
    }
    
    /**
     *  Called when a message for the DeliveryServiceUser has arrived.
     *
     *  @param message The message, that has arrived for the user.
     */
    public void handleMessage( KaaribogaMessage message ){
        synchronized ( this ){
	    inMessage = message;
	    statusInMessage = IN_MESSAGE_ARRIVED;
	    notify();
	}
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
        synchronized ( this ){
	    statusInMessage = IN_MESSAGE_ERROR;
	    notify();
	}
    }

}

