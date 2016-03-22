package raf.principal;



public interface RaListener extends java.util.EventListener{

   
    public void raDispatchRequest(RaEvent e);

  
    public void raSleepRequest(RaEvent e);

  
    public void raDestroyRequest(RaEvent e);
}