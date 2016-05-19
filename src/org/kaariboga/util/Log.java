package org.kaariboga.util;

import java.io.*;
import java.text.*;
import java.util.*;


/**
 *  Global handling of logging and error messages.
 *  This class is purely static.
 *  Don't create any instance of it. Just use it like:
 *  Log.write( Log.INFORMATION, "All is fine." );
 *  This class supports the following log levels:
 *  ERROR, WARNING, SUCCESS, INFORMATION, DEBUG.
 *  On startup loglevel INFORMATION is active.
 *  Note, that all errors in this class are written to 
 *  System.err. This prevents  infinite loops.
 */
public class Log
{
    /**
     *  Loglevel, if no messages should be logged
     */
    public final static int NONE = 0;    
    
    /**
     *  Used to write an error message
     */
    public final static int ERROR = 1;    

    /**
     *  Used to write an warning message
     */
    public final static int WARNING = 2;    

    /**
     *  Used to write a success message.
     *  A success denotes an operation that has been fullfilled
     *  whithout any errors. This is usefull if you have a monitor
     *  that should display if the current status is error or success.
     */    
    public final static int SUCCESS = 3;
       
    /**
     *  Used to write a normal status message
     */    
    public final static int INFORMATION = 4;

    /**
     *  Used to write a debug message for extended information
     */    
    public final static int DEBUG = 5;

   
    /**
     *  Name of the logfile
     */
    protected static String logfile;

    /**
     *  Name of the logfile backup
     */
    protected static String backupLogfile;

    /**
     *  maximum size of the logfile in bytes,
     *  standard is 100000
     */
    protected static long maxLogSize = 100000;

    /**
     *  Is there a logfile to write to?
     */   
    protected static boolean hasLogfile = false;
 
    /**
     *  should messages be written to standard output?
     */
    protected static boolean standardOut = true;

    /**
     *  Logging requests are fullfilled up to this loglevel.
     */
    protected static int loglevel = INFORMATION;

    /**
     *  OS dependent line separator
     */
    protected static String lineSeparator = System.getProperty( "line.separator" );

    /**
     *  date format
     */
    protected static SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd;HH:mm:ss;" );
   

    /**
     *  Creates a new Log object that uses logfile as output.
     *  If one of the parameters is null, no logfiles will be written.
     *  Default is no logfiles.
     *
     *  @param log Name of the logfile
     *  @param back Name as which the logfile should be stored, when it extends maximum size
     */
    public static void setLog( String log, String back ){
        if (log == null || back == null){
            hasLogfile = false;          
        }
        else {
            // XXX check for correct file name here
            logfile = log;
            backupLogfile = back;
            hasLogfile = true;
        }
    }


    /**
     *  Sets the loglevel of this Log.
     *  This makes it possible to supress certain kinds of log messages.
     *
     *  @param level Loglevel up to which the messages are logged.
     *                 May have the following values:
     *                 NONE        if no messages should be displayed,
     *                 ERROR       for error messages,
     *                 WARNING     for wanrings that are not critical,
     *                 SUCCESS     if an operation has been successfully finished,
     *                 INFORMATION for general information,
     *                 DEBUG       for extended debug information.
     */
    public static synchronized void setLoglevel( int level ){
        loglevel = level;
    }


    /**
     *  Returns the loglevel of this Log.
     *
     *  @return Loglevel up to which the messages are logged.
     *          One of the following values:
     *                 NONE        if no messages should be displayed,
     *                 ERROR       for error messages,
     *                 WARNING     for wanrings that are not critical,
     *                 SUCCESS     if an operation has been successfully finished,
     *                 INFORMATION for general information,
     *                 DEBUG       for extended debug information.
     */
    public static synchronized int getLoglevel(){
        return loglevel;
    }


   /**
     *  Writes a logging message.
     *
     *  @param status  Status of the message.
     *                 May have the following values:
     *                 ERROR       for error messages,
     *                 WARNING     for wanrings that are not critical,
     *                 SUCCESS     if an operation has been successfully finished,
     *                 INFORMATION for general information,
     *                 DEBUG       for extended debug information.
     *  @param message Text of the message.
     */
    public static synchronized void write( int status, String message ){
        String outMessage = null;                
        if ( status > loglevel ) return;
        switch (status){
            case ERROR:       outMessage = "Error;" + formatter.format( new Date() ) + message;
                              break;
            case WARNING:     outMessage = "Warning;" + formatter.format( new Date() ) + message;
                              break;
            case SUCCESS:     outMessage = "Success;" + formatter.format( new Date() ) + message;
                              break;
            case INFORMATION: outMessage = "Information;" + formatter.format( new Date() ) + message;
                              break;
            case DEBUG:       outMessage = "Debug;" + formatter.format( new Date() ) + message;
                              break;
            default: return;
        }                            
        if (standardOut == true){
            System.out.println( outMessage );
        }
        // write to logfile
        if (hasLogfile == true){
            FileWriter os = null;
            checkFileLength();
            try {
                os = new FileWriter( logfile, true );
                os.write( outMessage );
                os.write( lineSeparator );
            }
            catch( IOException e ){
                System.err.println( "! Log.write() " + e );
            }
            try {  
                if (os != null) os.close();
            }
            catch( IOException e ){
                System.err.println( "! Log.write() " + e );
            }            
        }
    }


    /**
     *  Sets the maximum size of the logfile.
     *  If the logfile exceeds the maximum size, the
     *  current log file is renamed and the Log creates
     *  a new one.
     *
     *  @param size Maximum size of the logfile in KB
     */
    public static synchronized void setMaxLogSize( int size ){
        maxLogSize = size;
    }


    /**
     *  Determines if Java standard output should be used.
     *  Default value is true.
     *
     *  @param flag True, if standard output should be used
     */    
    public static synchronized void setStandardOutput( boolean flag ){
        standardOut = flag;
    }


    /**
     *  Checks the length of the logfile.
     *  If the logfile exeeds the specified maximum size,
     *  it deletes the old backup file and renames the
     *  current logfile to the backup file.
     */
    protected static void checkFileLength() {
        try {
            File log = new File( logfile );
            if ( log.length() > maxLogSize ){
                File bak = new File( backupLogfile );
                bak.delete();
                boolean success = log.renameTo( bak );
                if (success == false) throw new IOException( "Could not rename file" );
            }
        }
        catch( SecurityException e ){
            System.err.println( "! Log.checkFileLength() " + e );
        }
        catch( NullPointerException e ){
            System.err.println( "! Log.checkFileLength() " + e );
        }
        catch( IOException e ){
            System.err.println( "! Log.checkFileLength() " + e );
        }
    }


}





