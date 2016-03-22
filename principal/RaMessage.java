package raf.principal;

import java.io.Serializable;


public class RaMessage implements Serializable{
  
    public int version;

    
    public RaAddress recipient;

    public RaAddress sender;

   
    public String kind;

    
    public String content;

   
    public byte binary[];

   
    public RaMessage(RaAddress sender,
                            RaAddress recipient,
                            String kind,
                            String content,
                            byte binary[]){
        this.version = 0;
        this.sender = sender;
        this.recipient = recipient;
        this.kind = kind;
        this.content = content;
        this.binary = binary;
    }

    public String toString() {
    
    	String s = "RaMessage\nDesde: " + sender + "\nA: " + recipient +
		   "\nTipo: " + kind + "\nContenido: ";
	if( content != null ) return s + content;
	else return s + "[binario]";
    }
}

