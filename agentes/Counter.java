package org.kaariboga.agents;

import java.lang.Thread;

import org.kaariboga.core.Kaariboga;
import org.kaariboga.core.KaaribogaEvent;


/**
 * Demonstrates the handling of time consuming applications by
 * couning up to a given number.
 */
public class Counter extends Kaariboga
{
    /**
     * Used to end the thread.
     */
    volatile Thread shouldLive;

    /**
     * Count up to i.
     */
    int i;

    /**
     * Creates a new counter that counts up to a given number.
     */
    public Counter (String name){
        super("GoodCounter_" + name);
    }

    /**
     * The run method just counts and ends when the thread is set to null.
     * Transfered to another base it continues counting.
     */
    public void run(){
        shouldLive = Thread.currentThread();
        Thread current = Thread.currentThread();
        current.setPriority(4);
        while (shouldLive == current && i < 100000){
            ++i;
            System.out.println("Counting " + i);
            current.yield();
        }
        fireDestroyRequest();
    }

    /**
     * Called by the kaariboga base before the agent is dispatched to another base.
     * Used here to end the current thread.
     */
    public void onDispatch(){
        shouldLive = null;
    }

    /**
     * Called by the kaariboga base when this agent has to be destroyed.
     * Used to end the current thread.
     */
    public void onDestroy(){
        shouldLive = null;
    }
}
