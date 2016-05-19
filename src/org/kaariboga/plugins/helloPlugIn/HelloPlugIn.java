package org.kaariboga.plugins.helloPlugIn;

import java.io.Serializable;
import java.util.Properties;

import org.kaariboga.plugin.*;


/**
 *  An example for the implementation of a plug-in.
 */
public class HelloPlugIn
extends KbPlugIn
implements Serializable
{
    /**
     *  A class that provides a set of methods.
     */
    private HelloService service;

    /**
     *  Initialize the plug-in with name and a service name.
     *  A service name is an identifier, which can be used by agents
     *  to find a specific plug-in.
     *  The name is just a unique Id.
     *
     *  @param name Unique Id for this instance.
     */
    public HelloPlugIn( String name ){
        super( name );
        serviceNames = new String[] {"kaariboga.org/HelloService"};
        service = new HelloService();
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
     *  Set configuration options for this plug-in.
     *
     *  @param props Properties for this plug-in.
     */
    public void setProperties( Properties properties ){ }

} 




