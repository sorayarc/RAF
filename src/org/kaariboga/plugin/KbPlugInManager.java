package org.kaariboga.plugin;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;


import org.kaariboga.core.*;




/**
 *  Handles plug-ins that can be used to extend a Kaariboga server.
 *
 *  Plug-ins should be within the usual Java class path.
 */
public class KbPlugInManager
{

    /**
     *  List of all the plugIns that are currently loaded
     */
    LinkedList plugIns;

    /**
     *  The base this plug-in is connected to
     */
    KaaribogaBase base;


    /**
     *  @param base The base at which this plug-in manager should register services.
     *         The base is also needed to generate unique IDs for the plug-ins.
     */
    public KbPlugInManager( KaaribogaBase base ){
        plugIns = new LinkedList();
        this.base = base;
    }


    /**
     *  Loads a plug-in with a given name and a given set of properties.
     *  In this version the plug-ins loaded by the PlugInManager are not 
     *  executed as agents on the base, because they are not mobile.
     *  
     *  @param classname Name of the class to load
     *  @param props Properties for the plug-in, may be null
     *  @return The plug-in if it was loaded successfully.
     */
    public KbPlugIn loadPlugIn( String classname, Properties props )
    throws java.lang.Exception
    {
        try{
            String name = base.generateName(); 

            Class plugInClass = Class.forName( classname );       
            
            Class[] types = new Class[] { String.class };
            Constructor constructor = plugInClass.getConstructor( types );

            Object[] params = new Object[] {name};
            KbPlugIn plugIn = (KbPlugIn) constructor.newInstance( params );
            plugIn.setBase( base );
            plugIn.addKaaribogaListener( base );
            plugIn.addKaaribogaMessageListener( base );

            plugIn.setProperties( props );
            plugIns.add( plugIn );
            new Thread( plugIn ).start();            
            System.out.println("Plug-in Manager: Plug-in loaded: " + classname );
            return plugIn;
        }
        catch( java.lang.Exception e){
            System.out.println("! PlugInManager.loadPlugIn: " + e);
            throw e;
        }   
    }


    /**
     *  Loads a plug-in with a given name and a given set of properties.
     *  
     *  @param classname Name of the class to load
     *  @param propertiesFile Name of a properties file for the required plug-in
     *  @return The plug-in if it was loaded successfully.
     */
    public KbPlugIn loadPlugIn( String classname, String propertiesFile )
    throws java.lang.Exception
    {
        Properties props = null;
        if ( propertiesFile != null ){
            props = new Properties();    
            FileInputStream in = new FileInputStream( propertiesFile );
            props.load (in);
            in.close();
        }
        return loadPlugIn( classname, props);        
    }


    /**
     *  Adds the specified plug-in to the KaaribogaBase
     *  A plug-in is only active after it has been registered.
     *  If registration fails this method tries to undo the whole operation.
     *
     *  @param plugIn A plug-in to extend the KaaribogaBase
     */
    public boolean registerPlugIn( KbPlugIn plugIn ){
        boolean result = false;
        boolean globalResult = true;

        // add services to the base 
        // every plug-in must have at least one service name        
        String[] services = plugIn.getServiceNames();
        for (int i = 0; i < services.length; i++){
            result = base.addService( plugIn, services[i] );
            if (result == false)
                globalResult = false;
        }
 
        // add message types to a base
        String[] types = plugIn.getMessageTypes();
        if (types != null){
            for (int i = 0; i < types.length; i++){
                result = base.addMessageTypeHandler( plugIn, types[i] );
                if (result == false)
                    globalResult = false;
            }        
        }

        if (globalResult == false){
            unregisterPlugIn( plugIn );
            return false;
        }
        else
            return true;
    }


    /**
     *  Removes the specified plug-in to the KaaribogaBase
     *
     *  @param plugIn A plug-in to extend the KaaribogaBase
     */
    public boolean unregisterPlugIn( KbPlugIn plugIn ){
        boolean result = false;
        boolean globalResult = true;

        // remove services from the base 
        // every plug-in must have at least one service name        
        String[] services = plugIn.getServiceNames();
        for (int i = 0; i < services.length; i++){
            result = base.removeService( plugIn, services[i] );
            if (result == false)
                globalResult = false;
        }
 
        // remove message types from the base
        String[] types = plugIn.getMessageTypes();
        if (types != null){
            for (int i = 0; i < types.length; i++){
                result = base.removeMessageTypeHandler( plugIn, types[i] );
                if (result == false)
                    globalResult = false;
            }        
        }

        if (globalResult == false)
            return false;
        else
            return true;
    }


    /**
     *  Returns the currently loaded plug-ins
     */
    public Collection getPlugIns(){
        return plugIns;
    }

} 









