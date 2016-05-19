package org.kaariboga.core;

import java.io.*;
import java.lang.ClassLoader;
import java.net.InetAddress;


import org.kaariboga.io.*;
import org.kaariboga.util.Log;


/**
 * Reades a kaariboga objcect with the class file from the stream.
 */
public class KaaribogaInputStream extends ObjectInputStream{

    /**
     *  KaaribogaBase that invokes this input stream.
     */
    KaaribogaBase base;

    /**
     * The internet host that supplies the byte codes for
     * the incoming agents.
     */
    KaaribogaAddress host;

    /**
     * Handles classes and their byte codes.
     */
    ClassManager classManager;

    /**
     *  Time in millis how long to wait for a class
     */
    long timeout;

    /**
     *  The deliveryService is used to send messages
     */
    DeliveryService deliveryService;

    /**
     * Creates a new KaaribogaInputStream.
     * This stream is a processing stream that has to be
     * connected to another stream.
     *
     * @param clManager Class manager, that manages the byte code of classes
     * @param base The KaaribogaBase, that creates this input stream
     * @param in The stream that this stream is connected to.
     * @param host The host that holds the source code for the incoming classes.
     * @param timeout Time in millis how long to wait for the loading of a class
     * @param deliveryService Service to post outgoing messages
     */
    public KaaribogaInputStream( ClassManager clManager,
                                 KaaribogaBase base,
				 InputStream in,
				 KaaribogaAddress host,
				 long timeout,
				 DeliveryService deliveryService )
    throws IOException{
        super(in);
        this.base = base;
        this.host = host;
        this.classManager = clManager;
	this.timeout = timeout;
	this.deliveryService = deliveryService;
    }

    /**
     * This is called by the super class if a new class is read in.
     * Resolve class calls the KaaribogaClassLoader to load the source
     * code of the class from a remote location if necessary.
     */
    protected Class resolveClass(ObjectStreamClass v)
    throws IOException, ClassNotFoundException{

	try { return super.resolveClass(v); }
	catch( Exception e ) {};

	Class result = null;

        KaaribogaClassLoader loader = new KaaribogaClassLoader( classManager,
	                                                        base,
								host,
								timeout,
								deliveryService );
        Log.write( Log.INFORMATION, "KaaribogaInputStream.resolveClass: loading class " + v.getName() );
        result = Class.forName( v.getName(), true, loader );
        Log.write( Log.INFORMATION, "Class loaded" );
	return result;
    }
}
