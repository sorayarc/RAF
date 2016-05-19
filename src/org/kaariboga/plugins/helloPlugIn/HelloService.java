package org.kaariboga.plugins.helloPlugIn;

import java.io.Serializable;

/**
 *  An example for a service class.
 *  A plug-in provides a set of methods.
 *  These methods are implemented in a service class.  
 */
public class HelloService
implements Serializable
{
    public void sayHello(){
        System.out.println( "Hello Plug-in" );
    }

}
