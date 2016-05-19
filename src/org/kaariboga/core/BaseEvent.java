package org.kaariboga.core;

/**
 * Events that can be fired by the kaariboga base.
 * These events are fired when a kaariboga agent is added or
 * removed from the base.
 *
 * @author Dirk Struve
 */
public class BaseEvent extends java.util.EventObject
{
    /**
     * Name of the kaariboga that has been added or removed from the base.
     */
    private String name;

    /**
     * Creates a new base event.
     *
     * @param obj The object that created the event.
     * @param name Name of the kaariboga.
     */
    public BaseEvent(Object obj, String name){
        super(obj);
        this.name = name;
    }

    /**
     * Returns the name of the kaariboga that has been added or
     * removed from the base.
     */
    public String getName(){
        return name;
    }
}