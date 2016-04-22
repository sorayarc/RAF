package raf.principal;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.security.CodeSource;
import java.security.SecureClassLoader;




public class RaClassLoader extends SecureClassLoader{

   
    ClassManager classManager;


    RaAddress agency;

   
    RaAddress sourceHost;


  

    public RaClassLoader(ClassManager clManager, RaAddress agency, RaAddress sourceHost){
        this.sourceHost = sourceHost;
        this.agency = agency;
        this.classManager = clManager;
    }


 
    protected byte loadClassData(String name)[]
    throws ClassNotFoundException{
        byte result[] = null;
        Socket socket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        System.out.println("RaClassLoader.loadClassData() ha sido llamado por: " + name);

        try{
            socket = new Socket(sourceHost.host, sourceHost.port);

            System.out.println("el cargador ha establecido la conexión");

            ObjectOutputStream outStream = new ObjectOutputStream(
                        socket.getOutputStream());
            ObjectInputStream inStream = new ObjectInputStream(
                    new BufferedInputStream(
                        socket.getInputStream()));

            RaMessage message = new RaMessage(agency,
                                                            sourceHost,
                                                            "GET_CLASS",
                                                            name, null);

            outStream.writeObject(message);
            outStream.flush();

            RaMessage inMessage = (RaMessage) inStream.readObject();
            result = inMessage.binary;

            inStream.close();
            outStream.close();
            socket.close();
        }
        catch (UnknownHostException e) {
            System.err.println("Funcion enviar: Host desconocido!");
        }
        catch (IOException e){
            System.err.println ("Funcion enviar: IOException!");
        }
        return result;
    }

   
    public Class findClass(String name)
    throws ClassNotFoundException {
    
        System.out.println("Ha sido llamado findClass en RaClassLoader!! " + name);


	Class c = classManager.getClass(name);
        if (c != null) {
            System.out.println("Recuperando la clase desde cache: " + name);
            classManager.inc(name);
            return c;
        }
	
	if (sourceHost == null) throw new ClassNotFoundException(name);
	byte[] data = loadClassData(name);
        
        if (data==null) throw new ClassNotFoundException(name);
	try {
	    System.out.println("Intentando definir la Clase: " + name + " tamaño: " + data.length);
  
         
            URL srcURL = new URL ("http", sourceHost.host.getHostAddress(), sourceHost.port, "/");
            CodeSource codeSrc = new CodeSource (srcURL, null); 
            c = defineClass (name, data, 0, data.length, codeSrc);
        }
	catch (java.net.MalformedURLException e) {
	    System.out.println("Secure Class Loader: URL mal formada!");
	    throw new ClassNotFoundException(name);
	}

        if (c==null) throw new ClassNotFoundException(name);
        
	System.out.println("Clase definida con exito");
        classManager.addClass(name, c, data);

        return c;
    }

}
