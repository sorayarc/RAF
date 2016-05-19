package org.kaariboga.plugins.domainPlugIn;

import java.io.Serializable;
import java.util.*;


/**
 *  Service class that is provided by the plug-in.
 *  This is a simple service to determine other servers
 *  in a domain.
 */
public class DomainService
implements Serializable
{
   
    protected DomainPlugIn parent;


    public DomainService( DomainPlugIn par ){
        this.parent = par;
    }

    /**
     *  @return A hashtable containing server names and KaaribogaAddresses.
     *          Each address identifies a server in the domain.
     */    
    public Hashtable getServers(){
        return parent.servers;
    }

}




