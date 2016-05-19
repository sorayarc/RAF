package org.kaariboga.util;


import java.lang.*;


/**
 *  A simple timer.
 *  This timer calls onSimpleTimer() of a SimpleTimerListener in regular time intervals.
 *  @see SimpleTimerListener
 */
public class SimpleTimer extends Thread
{
    /**
     *  interval to wait between notifications
     */
    protected long interval;

    /**
     *  The class, that is notified by this timer.
     */
    protected SimpleTimerListener listener;

    /**
     *  used to control, if this thread should terminate
     */
    protected volatile boolean shouldLive = true;
    
    /**
     *  used to indicate if this timer has been reset
     */
    protected volatile boolean reset;
    
    
    /**
     *  Creates a new Timer that notifies it's listener once in every interval
     *
     *  @param interval Interval in milliseconds this timer notifies the listener
     *  @param startImmeiately Determines if the timer should start or wait for the
     *                         first interval before calling onSimpleTimer.
     *  @param agent The agent, that is notified by this timer
     */
    public SimpleTimer( long interval, boolean startImmediately, SimpleTimerListener listener ){
        this.interval = interval;
        if (startImmediately == true) reset = false;
        else reset = true;
        this.listener = listener;
    }

    /**
     *  Just waits a specified time and notifies the listener.
     */
    public void run () {
        while (shouldLive){
            try {                
                if (reset) reset = false;
                else listener.onSimpleTimer();
                Thread.sleep( interval );                    
            }
            catch( InterruptedException e ){
                // this happens, if another class calls interrupt() or reset()
                // just continue, the while loop checks if this thread should live
            }
        }
    }

    /**
     *  Resets this timer.
     *  If the timer has been reset, it starts again waiting the given time interval
     */
    public void reset(){
        reset = true;
        interrupt();          
    }
    
    /**
     *  Terminates this timer.
     *  Be careful. This method returns, before the thread is terminated.
     *  If you want to wait for total termination :-), use something like this:
     *  myThread.terminate();
     *  myThread.interrupt(); // needed if thread is sleeping
     *  myThrad.join();
     */
    public void terminate() {
        shouldLive = false;
        interrupt();
    }
  
    
}


