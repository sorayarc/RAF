package org.kaariboga.core;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Defines an address for agents.
 * The address can be used to contact agents on other servers
 * around the world.
 */
public final class KaaribogaAddress implements Serializable{
    
    /**
     * The host where the agent exists.
     */
    public InetAddress host;
    
    /**
     * The port on which the server is listening.
     */
    public int port;

    /**
     * The name of the agent.
     * This in unique around the whole world.
     */
    public String name;
    
    /**
     * Constructs a new address.
     *
     * @param host The host where the agent exists.
     * @param port The port on which the server is listening.
     * @param name The name of the agent.
     */
    public KaaribogaAddress(InetAddress host, int port, String name){
        this.host = host;
        this.port = port;
        this.name = name;
    }
    
    /**
     * Constructs a new address.
     *
     * @param hostname The host where the agent exists.
     * @param port The port on which the server is listening.
     * @param name The name of the agent.
     */
/*   conflicts with dynamic IP, where KaaribogaAddress must be called
     with first parameter null. The JVM does not know then which KaaribogaAddress
     to call. 
     public KaaribogaAddress(String hostname, int port, String name) throws UnknownHostException{
        host = InetAddress.getByName (hostname);
        this.port = port;
        this.name = name;
    }    
*/

    /**
     * Constructs a new address for use on the local base server only.
     * So hostname and port are not required.
     *
     * @param name The name of the agent.
     */
    public KaaribogaAddress (String name) {
        host = null;
        this.port = 0;
        this.name = name;
    }    
    
    /**
     *  Definition for String conversion
     *
     *  @return A String that contains hostname, port and name of the agent. 
     */
    public String toString() {
    	if(host!=null) return host.toString() + ":" + port + ":" + name;
	return "local:"+ name;
     }    
    
}
