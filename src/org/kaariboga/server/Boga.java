package org.kaariboga.server;

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
import java.util.StringTokenizer; 
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;

import org.kaariboga.core.*;
import org.kaariboga.plugin.*;
import org.kaariboga.util.Log;


/**
 * This class implements a kaariboga server with a nice
 * user interface.
 */
public class Boga extends JFrame implements ActionListener,
                                            MenuListener,
                                            ListSelectionListener,
                                            BaseListener{
    /**
     * Where the settings for the server are.
     */
    String strConfigFile = "src"
    					  + File.separator
    					  +	"org"
                          + File.separator
                          + "kaariboga"
                          + File.separator
                          + "config"
                          + File.separator
                          + "boga.config";

    /** 
     *  String from the properties file, that determines
     *  the directories which include the agents 
     */ 
    String agentsPath;
 
    /**
     *  The directories which include the agents
     */
    String[] agentPaths;
    
    /**
     *  server that serves a valid IP address in case of
     *  dynamic IP addressing
     */ 
    String strIpServer;
    
    //
    // menues
    //
    JMenuBar menuBar;

    JMenu mFile;
    JMenuItem miLoad;
    JMenuItem miExit;
    
    JMenu mEdit;
    JMenuItem miSendTo;
    JMenuItem miDestroy;
    
    JMenu mCreate;
    JMenuItem miCreateHelloAgent;
    JMenuItem miCreateHelloTraveler;
    JMenuItem miCreateReturnAgent;
    JMenuItem miCreateCounter;
    JMenuItem miCreateExecuter;
    JMenuItem miCreateMessageReceiver;
    JMenuItem miCreateMessageSender;
    JMenuItem miCreateReproducer;
    
    JMenu mServer;
    JMenuItem miGoOnline;
    JMenuItem miGoOffline;
    
    JFileChooser fileChooser;
    JPanel panel = new JPanel();

    JList list;
    DefaultListModel listModel;
    JScrollPane listScroller;

    /**
     * Manages the byte codes of the loaded classes.
     */
    ClassManager classManager;

    /**
     * Name of the agent that was selected in the list.
     */
    String selectedKaariboga = null;

    /**
     * The base that handles all kaaribogas.
     */
    private KaaribogaBase kaaribogaBase;
    
    /**
     *  PlugInManager to handle the loaded plug-ins
     */
    private KbPlugInManager plugInManager;

    /**
     * Address of the server that registers all servers in domain.
     */
    KaaribogaAddress boServer;

    /**
     * Address of the server that determines the IP address of this
     * server in case of dynamic IP
     */
    KaaribogaAddress ipServer;
        
    /**
     * Port number for connections
     */
    int port;

    /** 
     * Address of the local Kaariboga base
     */ 
    KaaribogaAddress localBaseAddress = null; 

    /**
     *  Properties defined in Boga.config
     */
    Properties props;
    
    /**
     * Create a new Boga server.
     */
    public Boga (){
        super("Boga Server");

        long byteCodeDelay;
        String strBoServer = null;
        int boPort;

        props = new Properties ();

        // Read in properties from file
        try {
            FileInputStream in = new FileInputStream (strConfigFile);
            props.load (in);
            in.close();
        }
        catch (FileNotFoundException e){
            System.err.println ("! Boga: Could not open config file! " + e);
        }
        catch (IOException e){
            System.err.println ("! Boga: Reading config file failed! " + e);
        }

        Log.setLog( props.getProperty("logFileName"), props.getProperty("logFileNameBak") );

        try {
            int maxLogSize = Integer.parseInt( props.getProperty("maxLogSize", "100000") );
            Log.setMaxLogSize( maxLogSize );
        }
        catch( NumberFormatException e ) {
            Log.write( Log.ERROR, "Boga.Main() LogFile size not specified correctly " + e );
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

        Log.write( Log.INFORMATION, "Boga.Main: Begin initialisation." );


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
            strIpServer = props.getProperty("ipServer");
            strBoServer = props.getProperty("boServer");
            boPort = Integer.parseInt(props.getProperty("boPort", "10102"));
        }
        catch (NumberFormatException e){
            boPort = 10102;
        }

        try{
         // Try to find the address of the domain server
            if (strBoServer == null){
                boServer = null;
            }
            else{
                InetAddress server = InetAddress.getByName (strBoServer);
                boServer = new KaaribogaAddress (server, boPort, null);
            }


         // Try to find the address of the IP address server
            if (strIpServer == null){
                ipServer = null;
            }
            else{
                InetAddress server = InetAddress.getByName (strIpServer);
                ipServer = new KaaribogaAddress (server, boPort, null);
            }
        }
        catch (UnknownHostException e){
            Log.write( Log.ERROR, "Boga.Boga() " + e );
            boServer = null;
        }

        Log.write( Log.INFORMATION, "port: " + port);

        // convert agentsPath to list of directories
        agentsPath = props.getProperty ("agentsPath");
        StringTokenizer st = new StringTokenizer( agentsPath, File.pathSeparator );
    	agentPaths = new String[ st.countTokens() ];
        for (int i = 0; st.hasMoreTokens(); i++)  {
            agentPaths[i] = new String( st.nextToken() );
        }

        // set up new base
        classManager = new ClassManager( byteCodeDelay, agentPaths );
        kaaribogaBase = new KaaribogaBase( this, classManager, port );
        kaaribogaBase.addBaseListener( this );
        goOnline();

        // load the required plug-ins
        plugInManager = new KbPlugInManager( kaaribogaBase );
        KbPlugIn plugIn = null;
        String className = props.getProperty( "plugIn.0" );
        String configFile = props.getProperty( "plugIn.0.configFile" );
        int i = 0;
        while ( className != null ){
            try {
                plugIn = plugInManager.loadPlugIn( className, configFile );
                plugInManager.registerPlugIn( plugIn );
                i++;
                className = props.getProperty( "plugIn." + i );
                configFile = props.getProperty( "plugIn." + i + ".configFile" );
            }
            catch( Exception e){
                Log.write( Log.ERROR, "Boga.Boga(): loading plug-in failed " + e );
                i++;
                className = props.getProperty( "plugIn." + i );
                configFile = props.getProperty( "plugIn." + i + ".configFile" );
            }
        }


        setVisible (false);

        fileChooser = new JFileChooser ( agentPaths[0] );

       	// create main menu
       	menuBar = new JMenuBar();
       	setJMenuBar (menuBar);

       	// File menu
       	mFile = new JMenu ("File");

       	miLoad = new JMenuItem ("Load...");
        miLoad.setActionCommand( "loadFromFile" );
        miLoad.addActionListener (this);
        mFile.add (miLoad);

        mFile.addSeparator();

       	miExit = new JMenuItem ("Exit");
        miExit.setActionCommand( "exit" );
        miExit.addActionListener (this);
        mFile.add (miExit);

      	menuBar.add (mFile);


       	// Edit menu
       	mEdit = new JMenu ("Edit");
        mEdit.addMenuListener (this);

       	miSendTo = new JMenuItem ("Send To...");
        miSendTo.setActionCommand( "editSendTo" );
       	miSendTo.addActionListener (this);
       	mEdit.add (miSendTo);

       	miDestroy = new JMenuItem ("Destroy");
        miDestroy.setActionCommand( "editDestroy" );
       	miDestroy.addActionListener (this);
       	mEdit.add (miDestroy);

       	menuBar.add (mEdit);


        // Create Menu
        mCreate = new JMenu ("Create");

       	miCreateHelloAgent = new JMenuItem ("Hello Agent");
        miCreateHelloAgent.setActionCommand( "createHelloAgent" );
       	miCreateHelloAgent.addActionListener (this);
       	mCreate.add (miCreateHelloAgent);

       	miCreateHelloTraveler = new JMenuItem ("Hello Traveler");
       	miCreateHelloTraveler.setActionCommand( "createHelloTraveler" );
       	miCreateHelloTraveler.addActionListener (this);
       	mCreate.add (miCreateHelloTraveler);

       	miCreateReturnAgent = new JMenuItem ("Return Agent");
       	miCreateReturnAgent.setActionCommand( "createReturnAgent" );
       	miCreateReturnAgent.addActionListener (this);
       	mCreate.add (miCreateReturnAgent);

       	miCreateCounter = new JMenuItem ("Counter");
       	miCreateCounter.setActionCommand( "createCounter" );
       	miCreateCounter.addActionListener (this);
       	mCreate.add (miCreateCounter);

       	miCreateExecuter = new JMenuItem ("Executer");
        miCreateExecuter.setActionCommand( "createExecuter" );
       	miCreateExecuter.addActionListener (this);
       	mCreate.add (miCreateExecuter);

       	miCreateMessageReceiver = new JMenuItem ("MessageReceiver");
       	miCreateMessageReceiver.setActionCommand( "createMessageReceiver" );
       	miCreateMessageReceiver.addActionListener (this);
       	mCreate.add (miCreateMessageReceiver);

       	miCreateMessageSender = new JMenuItem ("MessageSender");
       	miCreateMessageSender.setActionCommand( "createMessageSender" );
       	miCreateMessageSender.addActionListener (this);
       	mCreate.add (miCreateMessageSender);

       	miCreateReproducer = new JMenuItem ("Reproducer");
        miCreateReproducer.setActionCommand( "createReproducer" );
       	miCreateReproducer.addActionListener (this);
       	mCreate.add (miCreateReproducer);

       	menuBar.add (mCreate);

        // Server menu
       	mServer = new JMenu ("Server");
        mServer.addMenuListener (this);

       	miGoOnline = new JMenuItem ("Go Online");
       	miGoOnline.setActionCommand( "goOnline" );
        miGoOnline.addActionListener (this);
        mServer.add (miGoOnline);

       	miGoOffline = new JMenuItem ("Go Offline");
       	miGoOffline.setActionCommand( "goOffline" );
        miGoOffline.addActionListener (this);
        mServer.add (miGoOffline);

      	menuBar.add (mServer);


        // Frame contents
        getContentPane().add (panel);
        panel.setLayout (new GridLayout(1,1));
        panel.setPreferredSize(new java.awt.Dimension(500, 300));



        listModel = new DefaultListModel();
        list = new JList (listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        listScroller = new JScrollPane (list);

        panel.add (listScroller);

    }


    /**
     *  Call for a proper clean up of the network connections.
     */
    public void dispose() {
        goOffline();
    }


    /**
     * Create a new Boga server.
     * Boga server is a swing based server with a nice look and feel.
     */
    public static void main(String[] args){

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (Exception e) {
            System.err.println("Couldn't use the cross-platform look and feel: " + e);
        }

        JFrame frame = new Boga();
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ((Boga) e.getWindow()).dispose();
                System.exit(0);
            }
        };
        frame.addWindowListener(l);
        frame.pack();
        frame.setVisible(true);
    }

    //
    // Methods for the MenuListener    
    public void menuCanceled (MenuEvent evt) { }
    public void menuDeselected (MenuEvent evt) { }

    public void menuSelected (MenuEvent evt) {
        Object src = evt.getSource();
        boolean isEnabled;
        if ( src == mServer ) {
            miGoOnline.setEnabled ( !kaaribogaBase.getOnlineStatus() );
            miGoOffline.setEnabled ( kaaribogaBase.getOnlineStatus() );
        } 
        else if ( src == mEdit ) {
            if ( list.getSelectedIndex() == -1 )
                isEnabled = false;
            else 
                isEnabled = true;
            miSendTo.setEnabled  ( isEnabled );
            miDestroy.setEnabled ( isEnabled );
        } 
    }
    
    /**
     * Handles the menu events
     */
    public void actionPerformed (ActionEvent evt){
        String cmd = evt.getActionCommand();
        if ( cmd.equals( "loadFromFile" ) ) {
            loadFromFile();
        }
        else if ( cmd.equals( "exit" ) ) {
            dispatchEvent ( new WindowEvent(this, WindowEvent.WINDOW_CLOSING) );
        }
        else if ( cmd.equals( "editSendTo" ) ) {
            editSendTo();
        }
        else if ( cmd.equals( "editDestroy" ) ) {
            editDestroy();
        }
        else if ( cmd.equals( "createHelloAgent" ) ) {
            createHelloAgent();
        }
        else if ( cmd.equals( "createHelloTraveler" ) ) {
            createHelloTraveler();
        }
        else if ( cmd.equals( "createReturnAgent" ) ) {
            createReturnAgent();
        }
        else if ( cmd.equals( "createCounter" ) ) {
            createCounter();
        }
        else if ( cmd.equals( "createExecuter" ) ) {
            createExecuter();
        }
        else if ( cmd.equals( "createMessageReceiver" ) ) {
            createMessageReceiver();
        }
        else if ( cmd.equals( "createMessageSender" ) ) {
            createMessageSender();
        }
        else if ( cmd.equals( "createReproducer" ) ) {
            createReproducer();
        }
        else if ( cmd.equals( "goOnline" ) ) {
            goOnline();
        }
        else if ( cmd.equals( "goOffline" ) ) {
            goOffline();
        }
    }


    /** 
     * Loads a kaariboga agent from a file 
     */ 
    public void loadFromFile () {
        try{ 
            String name; 
 
            int returnVal = fileChooser.showOpenDialog (this); 
            if (returnVal == JFileChooser.APPROVE_OPTION) { 
                name = fileChooser.getSelectedFile().getCanonicalPath(); 
     
                // look for the directory the agent is in 
                // and strip agent path from file path 
                // This only works if an angent directory is no
                // subdirectory of another agent directory.
                for (int i = 0; i < agentPaths.length; i++) {
                    if ( name.startsWith( agentPaths[i] ) ) {
                        name = name.substring ( agentPaths[i].length() ); 
                        break;
                    }
                } 

                // strip file suffix
                int suffixPos = name.lastIndexOf ('.'); 
                name = name.substring ( 0, suffixPos );             
 
                // convert path to packet name 
                name = name.replace (File.separatorChar, '.'); 

                loadKaariboga (name);
            }
        }
        catch (IOException e){ 
            Log.write( Log.ERROR, "Boga.loadFromFile(): " + e );
        } 
    }


    /**
     * Loads a kaariboga agent 
     *
     *  @param name classname of the agent, 
     *         for example org.kaariboga.agents.HelloAgent
     */
    public void loadKaariboga (String name){
        try{
            kaaribogaBase.loadKaariboga( this, name );
        }
        catch (InvocationTargetException e){
            Log.write( Log.ERROR, "Boga.loadKaariboga(): " + e );
        }
        catch (SecurityException e){
            Log.write( Log.ERROR, "Boga.loadKaariboga(): " + e );
        }
        catch (ClassNotFoundException e){
            Log.write( Log.ERROR, "Boga.loadKaariboga(): " + e );
        }
        catch (IllegalAccessException e){
            Log.write( Log.ERROR, "Boga.loadKaariboga(): " + e );
        }
        catch (InstantiationException e){
            Log.write( Log.ERROR, "Boga.loadKaariboga(): " + e );
        }
    }


    /**
     * Sends the selected kaariboga to another base.
     */
    void editSendTo(){
        InetAddress destination = null;
        String server = null;
        String servername = null;
        String strLoPort = null;
        int loPort = port;

        try{
            // open dialog for the input of the server address
            JOptionPane dialog = new JOptionPane();
            server = dialog.showInputDialog (null, "Internet Address");
            if (server == null) return;
            
            // split server address into servername and port and determine host address
            server.trim();
            int portDelimiter = server.indexOf (':');
            if (portDelimiter != -1) {
                servername = server.substring (0, portDelimiter);
                strLoPort  = server.substring (portDelimiter + 1);
                loPort = Integer.parseInt (strLoPort);
            }   
            else{
                servername = server;          
            }
            destination = InetAddress.getByName (servername);
            kaaribogaBase.dispatchKaariboga (this, selectedKaariboga, new KaaribogaAddress (destination, loPort, null));
        }
        catch (IndexOutOfBoundsException e){
            Log.write( Log.ERROR, "Boga.editSendTo: Wrong format for port " + e );
        }
        catch (NumberFormatException e){
            Log.write( Log.ERROR, "Boga.editSendTo: Wrong format for port " + e );
        }
        catch (UnknownHostException e){
            Log.write( Log.ERROR, "Boga.editSendTo: Could not determine host address " + e );
        }
    }


    /**
     * Destroys the selected kaariboga.
     */
    void editDestroy(){
        kaaribogaBase.destroyKaariboga (this, selectedKaariboga);
    }

    /**
     *  Causes the base to go online
     */
    void goOnline(){
        Log.write( Log.INFORMATION, "BogaServer: Going Online " + port );
        kaaribogaBase.goOnline (this, port, boServer, ipServer);
        localBaseAddress = kaaribogaBase.getBaseAddress (this);
    }

    /**
     *  Causes the base to go offline
     */
    void goOffline(){
        Log.write( Log.INFORMATION, "BogaServer: Going Offline");
        kaaribogaBase.goOffline (this);
        localBaseAddress = null;
    }

    /**
     * Generates a HelloAgent and puts it to the base.
     */
    void createHelloAgent(){
        loadKaariboga ("org.kaariboga.agents.HelloAgent");
    }

    /**
     * Generates a HelloTraveler and puts it to the base.
     */
    void createHelloTraveler(){
        loadKaariboga ("org.kaariboga.agents.HelloTraveler");
    }

    /**
     * Creates a ReturnAgent and puts it to the base.
     */
    void createReturnAgent(){
        loadKaariboga ("org.kaariboga.agents.ReturnAgent");
    }

    /**
     * Creates a Counter and puts it to the base.
     */
    void createCounter(){
        loadKaariboga ("org.kaariboga.agents.Counter");
    }

    /**
     * Creates an Executer and puts it to the base.
     */
    void createExecuter(){
        loadKaariboga ("org.kaariboga.agents.Executer");
    }

    /**
     * Creates a MessageReceiver and puts it to the base.
     */
    void createMessageReceiver(){
        loadKaariboga ("org.kaariboga.agents.MessageReceiver");
    }

    /**
     * Creates an MessageSender and puts it to the base.
     */
    void createMessageSender(){
        loadKaariboga ("org.kaariboga.agents.MessageSender");
    }

    /**
     * Creates an Reproducer and puts it to the base.
     */
    void createReproducer(){
        loadKaariboga ("org.kaariboga.agents.Reproducer");
    }


    /**
     * Remembers the agent that was chosen from the list.
     */
    public synchronized void valueChanged (ListSelectionEvent e){
        int pos = list.getSelectedIndex();
	if ( pos != -1 ){
            selectedKaariboga = (String) listModel.elementAt (pos);
            Log.write( Log.INFORMATION, "selected: " + selectedKaariboga );
	}
	else selectedKaariboga = null;
    }

    /**
     * Reaction when a kaariboga agent has been put to a base and
     * the base invoked the agent's onCreate method.
     */
    public void baseKaaribogaCreated (BaseEvent e){
        listModel.addElement (e.getName());
    }

    /**
     * Reaction when a kaariboga agent has arrived on the base or
     * has been added on creation.
     */
    public void baseKaaribogaArrived (BaseEvent e){
        listModel.addElement (e.getName());
    }

    /**
     * Reaction when a kaariboga agent left the base.
     */
    public void baseKaaribogaLeft (BaseEvent e){
        listModel.removeElement ((String)e.getName());
    }

    /**
     * Reaction when a kaariboga agent has been destroyed.
     */
    public void baseKaaribogaDestroyed (BaseEvent e){
        listModel.removeElement ((String)e.getName());
    }


}






