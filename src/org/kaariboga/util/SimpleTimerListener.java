package org.kaariboga.util;


/**
 *  Classes who want some action to be triggered by a
 *  SimpleTimer should implement this interface.
 *
 *  @see SimpleTimer
 */
public interface SimpleTimerListener
{
    /** 
     *  This method is called by a SimpleTimer in regular intervals
     */
    public void onSimpleTimer();
    
}

