package org.kaariboga.domainserver;


import org.kaariboga.core.*;



/**
 *  Stores data of a KaaribogaBase, that is connected to the domain.
 */
public class BaseEntry
{
    /**
     *  Address of the server, that connects to the domain
     */
    public KaaribogaAddress base;

    /**
     *  Should be the hashkey, that is used to store this Entry in a hashtable.
     *  This is needed for better performance 
     */
    public String hashkey;

    /**
     *  Time in millis when this entry was created
     */
    public long creationTime;

    /**
     *  Last time in millis, when the server has reported being online
     */
    public long lastUpdate;

    /**
     *  Counts how often the server has allready reported being online
     */
    public long count;


    /**
     *  @param hashkey Hashkey, that is used to store this Entry in a hashtable.
     *  @param base Address of the server, that connects to the domain
     */
    public BaseEntry( String hashkey, KaaribogaAddress base ){
        this.hashkey = hashkey;
        this.base = base;
        creationTime = System.currentTimeMillis();
        lastUpdate = creationTime;
        count = 1;
    }


}


