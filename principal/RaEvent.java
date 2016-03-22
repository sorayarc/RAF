package raf.principal;


public class RaEvent extends java.util.EventObject
{
    
    public static final int DISPATCH_REQUEST = 1;
    
    
    public static final int SLEEP_REQUEST = 2;
    
   
    public static final int DESTROY_REQUEST = 3;
    
  
    protected int id;
    
   
    public RaEvent(Object obj, int id){
        super(obj); 
        this.id = id;
    } 
    
    
    public int getID(){
        return id;
    }    
}    