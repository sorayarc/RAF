package raf.principal;

import java.net.InetAddress;
import java.util.Date;


public class RaBox
{
   
    public Ra ra;
    
    
    public Thread thread;

    public Date timeOfArrival;

    public InetAddress sendingHost;

   
    public RaBox(Ra ra, Thread thread,
                      Date timeOfArrival, InetAddress sendingHost){
        this.ra = ra;
        this.thread = thread;
        this.timeOfArrival = timeOfArrival;
        this.sendingHost = sendingHost; 
    }    
}