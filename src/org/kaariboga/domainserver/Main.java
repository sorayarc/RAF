package org.kaariboga.domainserver;


import java.io.*;
import java.util.*;

import org.kaariboga.util.Log;


/**
 *  Reads in properties and starts the DomainServer
 */
public class Main
{

    /**
     *  Reads in properties and starts the domain server.
     *
     *  @param propertiesFile Filename of the configuration file.
     */          
    public Main( String propertiesFile ){
        if ( propertiesFile == null ){
            Log.write( Log.ERROR, "Properties file not specified." );
            System.exit(1);
        }
        
        int port = 10100;
        Properties props = new Properties();            
              
        // read in properties
        try {            
            FileInputStream in = new FileInputStream( propertiesFile );
            props.load( in );
            in.close();
        }                       
        catch( FileNotFoundException e ){
            Log.write( Log.ERROR, "Main.Main() Properties File not found " + e );
            System.exit(1);
        }
        catch( IOException e ){
            Log.write( Log.ERROR, "Main.Main() Reading config file failed! " + e );
            System.exit(1);
        }

        Log.setLog( props.getProperty("logFileName"), props.getProperty("logFileNameBak") );

        try {
            int maxLogSize = Integer.parseInt( props.getProperty("maxLogSize", "100000") );
            Log.setMaxLogSize( maxLogSize );
        }
        catch( NumberFormatException e ) {
            Log.write( Log.ERROR, "Main.Main() LogFile size not specified correctly " + e );
            System.exit(1);
        }        

        int loglevel = Log.INFORMATION;
        String strLoglevel = props.getProperty( "loglevel", "information" );
        if      ( strLoglevel.compareToIgnoreCase("none") == 0 ) loglevel = Log.NONE;
        else if ( strLoglevel.compareToIgnoreCase("error") == 0 ) loglevel = Log.ERROR;
        else if ( strLoglevel.compareToIgnoreCase("warning") == 0 ) loglevel = Log.WARNING;
        else if ( strLoglevel.compareToIgnoreCase("success") == 0 ) loglevel = Log.SUCCESS;
        else if ( strLoglevel.compareToIgnoreCase("information") == 0 ) loglevel = Log.INFORMATION;
        else if ( strLoglevel.compareToIgnoreCase("debug") == 0 ) loglevel = Log.DEBUG;                    
        else loglevel = Log.INFORMATION;

        Log.setLoglevel( loglevel );

        Log.write( Log.INFORMATION, "Main: Begin initialisation." );
             
        try {
            port = Integer.parseInt( props.getProperty("port", "10100") );
        }
        catch( NumberFormatException e ) {
            Log.write( Log.ERROR, "Main.Main() Port not specified correctly " + e );
            System.exit(1);
        }       

        long interval = 10000;
        try {
            interval = Long.parseLong( props.getProperty("timeInterval", "10000") );
        }
        catch( NumberFormatException e ) {
            Log.write( Log.ERROR, "Main.Main() timeInterval not specified correctly " + e );
            System.exit(1);
        }       

        new DomainServer( port, interval );
    }


    /**
     *  Starts the program.
     */
    public static void main( String[] args ){
        if (args.length == 1)
            new Main( args[0] );
        else {
            String strConfigFile = "src"
            					  + File.separator
            					  + "org"
                                  + File.separator
                                  + "kaariboga"
                                  + File.separator
                                  + "config"
                                  + File.separator
                                  + "domainserver.config";
 
            System.out.println( "Starting using default config file name: " + strConfigFile );
            new Main( strConfigFile );
        }
    }


} 




