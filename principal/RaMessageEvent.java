package raf.principal;


public class RaMessageEvent extends java.util.EventObject
{
   
    private RaMessage m;

    public RaMessageEvent(Object obj, RaMessage m){
        super(obj);
        this.m = m;
    }

   
    public RaMessage getMessage(){
        return m;
    }
}