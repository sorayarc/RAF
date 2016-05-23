package raf.principal;


public class RaMessageEvent extends java.util.EventObject
{
   
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RaMessage m;

    public RaMessageEvent(Object obj, RaMessage m){
        super(obj);
        this.m = m;
    }

   
    public RaMessage getMessage(){
        return m;
    }
}