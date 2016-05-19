package org.kaariboga.plugin;

import java.io.Serializable;
import java.util.Properties;

import org.kaariboga.core.*;


/**
 *  A plug-in provides the means to enhance a Kaariboga base.
 *
 */
public abstract class KbPlugIn
extends Kaariboga
implements Serializable
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String[] serviceNames;


    /**
     *  @param name Unique Id for this plug-in
     */
    public KbPlugIn( String name ){
        super( name );
    }


    /**
     *  Returns the names of the services this plug-in provides.
     */
    public String[] getServiceNames(){
        return serviceNames;
    }


    /**
     *  Each plug-in must provide an object, that implements the
     *  methods the plug in provides for external classes like agents.
     *  
     *  @param name Name of the requested service
     *  @return An object, that can be used to call the methods this plug-in provides
     */
    public abstract Object getService( String name );

    
    /**
     *  Get the message types, this plug-in can handle.
     *  The messages may be registered at a KaaribogaBase.
     *  The base will delegate appropriate messages to the
     *  right plug-in.
     *
     *  @return An array with the names of the messages
     */
    public String[] getMessageTypes(){
        return null;
    }

    
    /**
     *  Set configuration options for this plug-in.
     *
     *  @param props Properties for this plug-in.
     */
    public abstract void setProperties( Properties properties );
}








