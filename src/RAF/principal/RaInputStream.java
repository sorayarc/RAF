package raf.principal;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.IOException;
import java.lang.ClassLoader;
import java.net.InetAddress;



public class RaInputStream extends ObjectInputStream{
    
    
    RaAddress agency;
    
   
    RaAddress host;
    
    
    ClassManager classManager; 
    
    
    public RaInputStream(ClassManager clManager, RaAddress agency, InputStream in, RaAddress host) throws IOException{
        super(in);
        this.agency = agency;
        this.host = host;
        this.classManager = clManager;
    }

   
    protected Class resolveClass(ObjectStreamClass v) 
    throws IOException, ClassNotFoundException{
        Class result;
        RaClassLoader loader = new RaClassLoader(classManager, agency, host);
        System.out.println ("RaInputStream esta argando la clase " + v.getName());        
        result = loader.loadClass(v.getName());
        System.out.println ("Clase cargada");         
        return result;
    }
}
