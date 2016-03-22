package raf.servidor;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;

//import raf.agentes.*;
import raf.principal.*;


/**
 * Esta clase implementa un lanzador con un
 * interfaz de usuario.
 */
public class RaLauncher {
    /**
     * Donde estan las configuraciones para el servidor.
     */
    String strConfigFile = "raf"
                          + File.separator
                          + "config"
                          + File.separator
                          + "movil.config";

   
    /**
     * Manaja los byte codes de las clases cargadas.
     */
    ClassManager classManager;

   
	/**
	 * La base que maneja todos los agentes.
	 */
	private RaAgency raAgency;

    /**
     * Direccion del servidor que registra todos los servidores en el dominio.
     */
    RaAddress raServer;

    /**
     * Nº de puerto para las conexiones, por defecto 10101.
     */
    int port;

    /**
     * Crea un nuevo servidor Movil.
     * El servidor Movil es un servidor basado en swing.
     */
    public RaLauncher() {
        //super("RaLauncher");

        long byteCodeDelay;
        String strRaServer = null;
        int raPort;
        Properties props = new Properties ();

        // Lee las propiedades del fichero
        try {
            FileInputStream in = new FileInputStream (strConfigFile);
            props.load (in);
            in.close();
        }
        catch (FileNotFoundException e){
            System.err.println ("RaLauncher: No se pudo abrir el fichero de configuracion!");
        }
        catch (IOException e){
            System.err.println ("RaLauncher: Ha fallado la lectura del fichero de configuracion!");
        }

        try {
            port = Integer.parseInt(props.getProperty("port", "10101"));
        }
        catch (NumberFormatException e){
            port = 10101;
        }
        try {
            byteCodeDelay = Long.parseLong(props.getProperty("byteCodeDelay", "100000"));
        }
        catch (NumberFormatException e){
            byteCodeDelay = 100000;
        }
        try {
            strRaServer = props.getProperty("raServer");
            raPort = Integer.parseInt(props.getProperty("raPort", "10102"));
        }
        catch (NumberFormatException e){
            raPort = 10102;
        }

        try{
            if (strRaServer == null){
                raServer = null;
            }
            else{
                InetAddress server = InetAddress.getByName (strRaServer);
                raServer = new RaAddress (server, raPort, null);
            }
        }
        catch (UnknownHostException e){
            System.out.println("! RaLauncher: RaServer no valido." + e);
            raServer = null;
        }

        System.out.println ("port: " + port);

        // pone una base
        classManager = new ClassManager (byteCodeDelay, props.getProperty("agentsPath"));

        raAgency = new RaAgency (this, classManager);
       

        startAgency();
    }


    /**
     *  Llamada para hacer la limpieza de las conexiones de red.
     */
    public void dispose() {
        stopAgency();
    }


    /**
     * Crea un nuevo servidor Movil.
     */
    public static void main(String[] args){

        new RaLauncher();
    }

    
    /**
     * Inicializa el thread para la RaAgency.
     */
    void startAgency(){
	System.out.println ("Inicializando la Agencia");
        raAgency.startAgency (this, port, raServer);
    }

    /**
     * Para el Thread de la RaAgency.
     */
    void stopAgency(){
	System.out.println ("Parando la Agencia");
        raAgency.stopAgency (this);
    }

   
  

}

