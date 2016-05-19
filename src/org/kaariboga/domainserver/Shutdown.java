package org.kaariboga.domainserver;


import java.lang.*;

import org.kaariboga.util.Log;


/**
 *  Shuts down the domain server, if a user hits CTRL+C
 */
public class Shutdown
extends Thread 
{

    /**
     *  Server to shut down
     */
    DomainServer server;

    /**
     *  @param server A domain server, that should be shut down
     */
    public Shutdown( DomainServer server ){
        this.server = server;
    }

    /**
     *  This is called, when the program is terminated.
     *  Examples: The System is going down, or a user hits CTRL+C
     */
    public void run () {
        Log.write( Log.DEBUG, "Shutdown.run" );
        server.terminate();
    }


}

