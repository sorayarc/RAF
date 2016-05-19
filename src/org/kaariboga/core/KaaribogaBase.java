package org.kaariboga.core;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;


import org.kaariboga.io.*;
import org.kaariboga.plugin.*;
import org.kaariboga.util.Log;


/**
 * A class to host mobile agents called kaaribogas. It is able to dispatch
 * and receive kaaribogas, handle their requests and messages.
 */
public class KaaribogaBase
    implements Serializable, KaaribogaListener, KaaribogaMessageListener, KbMessageHandler
{
    /**
     *  time in millis how long to wait for class loading over network
     */
    private long timeout = 300000;

    /**
     * Not used.
     */
    public final int version = 0;

    /**
     *  Is this server on- or offline?
     *  Use GetOnlineStatus to retrieve this value.
     */
    private boolean isOnline = false;
    
    /**
     * The object that created this base.
     * This variable is used for security purposes.
     * The parent has more rights than other objects.
     */
    private Object parent;

    /**
     * All servers connected to this domain.
     */
    //xxx private Hashtable bases;

    /**
     *  Message handlers that have registered on this base to handle certain kinds of messages.
     */
    private HashMap messageTypeHandlers;

    /**
     *  Message handlers that have registered on this base, but are not agents.
     */
    private HashMap messageHandlers;

    /**
     *  The base can be extended by services, that are provided by plug-ins.
     */
    private HashMap services;


    /**
     * Base online server that registers all servers in this domain.
     */
    private KaaribogaAddress boServer = null;

    
    /**
     *  Server that serves dynamic IP addresses
     */
    private KaaribogaAddress ipServer = null;
        
    
    /**
     * Classes that listen to the fired base events.
     */
    private Vector baseListeners;

    /**
     * Delay time in milliseconds after which an anused agent
     * class is removed from memory.
     */
    long delay = 100000;

    /**
     * The port number on which the server listens.
     */
    private int port;

    /**
     * The KaaribogaAddress of this base
     */
    private KaaribogaAddress baseAddress = null;

    /**
     * Used to generate a unique name for a kaariboga.
     */
    private long counter = 0;

    /**
     * Thread that accepts network connections.
     */
    private volatile ListenThread listenThread = null;

    /**
     * Handles classes and their byte codes.
     */
    protected ClassManager classManager;

    /**
     * Stores all agents and related information.
     */
    Hashtable boxes;

    /**
     * Listens on the main port.
     */
    ServerSocket serverSocket = null;

    /**
     *  delivers messages across the network
     */
    DeliveryService deliveryService;


    /**
     *  Creats a new Kaariboga Base.
     *
     *  @param parent The class that created this base.
     *  @param clManager The ClassManager to use.
     *  @param port The network port on which to listen.
     */
    public KaaribogaBase( Object parent, ClassManager clManager, int port )
    {
        try {
            this.parent = parent;
            baseListeners = new Vector();
            boxes = new Hashtable();
            services = new HashMap();
            messageTypeHandlers = new HashMap();
            messageHandlers = new HashMap();
            classManager = clManager;
            deliveryService = new DeliveryService( this, port );
            if (ipServer == null)
                // no dynamic IP addressing
                baseAddress = new KaaribogaAddress (InetAddress.getLocalHost(), port, null);
            else
                baseAddress = null;
        }
        catch (UnknownHostException e) {
            System.out.println ("! KaaribogaBase.KaaribogaBase() " + e);
        }
    }


    /**
     * Handles incoming messages from other bases and kaaribogas.
     * At this time the function just prints out the message contents.
     */
    public void handleMessage( KaaribogaMessage message )
    {
        KaaribogaMessage outMessage = null;

        try {
            if ( baseAddress != null && !message.recipient.host.equals(baseAddress.host) ){
                // message relaying
		deliveryService.postMessage( message );
            }
            else if ( message.kind.equals("KAARIBOGA") ){
                // a Kaariboga agent has arrived
		Log.write( Log.INFORMATION, "KaaribogaBase.handleMessage: Message KAARIBOGA arrived." );
                ByteArrayInputStream bInStream = new ByteArrayInputStream(message.binary);
                KaaribogaInputStream mis = new KaaribogaInputStream( classManager,
		                                                     this,
		                                                     bInStream,
								     message.sender,
								     timeout,
								     deliveryService );
                Kaariboga agent = (Kaariboga) mis.readObject();
                addKaaribogaOnArrival( agent, message.sender.host );
            }
            // this is for dynamic IP addresses and only allowed before the
            // server is in normal online mode
            else if ( message.kind.equals("YOUR_IP_ADDRESS") && isOnline==false ){
                Log.write( Log.INFORMATION, "KaaribogaBase.handleMessage: Message YOUR_IP_ADDRESS arrived: " + message.recipient);
                baseAddress = message.recipient;
            }
            else if ( message.kind.equals("GET_CLASS") ){
                // another base is requesting byte code for an agent or class
		Log.write( Log.INFORMATION, "KaaribogaBase.handleMessage: Message GET_CLASS arrived: " + message.content + ".class");
                byte[] source = classManager.getByteCode (message.content);
                outMessage = new KaaribogaMessage( baseAddress,
                                                   message.sender,
                                                   "CLASS",
                                                   message.content,
                                                   source );
                deliveryService.postMessage( outMessage );
            }
            else if (message.kind.equals("GET")){
                // another base is requesting an agent
		Log.write( Log.INFORMATION, "KaaribogaBase.handleMessage: Message GET arrived: " + message.content );
		KaaribogaBox target = (KaaribogaBox) boxes.get(message.content);
                if (target != null){
                    // send kaariboga back
                    target.kaariboga.onDispatch();
                    target.thread = null;

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject (target.kaariboga);

                    outMessage = new KaaribogaMessage( baseAddress,
                                                       message.sender,
                                                       "KAARIBOGA",
                                                       message.content,
                                                       bos.toByteArray() );
                    deliveryService.postMessage( outMessage );

                    // clean up
                    boxes.remove(message.content);
                    classManager.dec(message.content);
                }
                else {
                    // reply ERROR message
                    outMessage = new KaaribogaMessage( baseAddress,
                                                       message.sender,
                                                       "ERROR",
                                                       "Kaariboga not found!",
                                                        null );
                    deliveryService.postMessage( outMessage );
                }
            }
            else if (message.recipient.name!=null) {
                // deliver message to local Kaariboga agent
		Log.write( Log.INFORMATION, "KaaribogaBase.handleMessage: Try to deliver message to local agent: " + message.recipient.name );
                KaaribogaBox box = (KaaribogaBox) boxes.get(message.recipient.name);
                if (box != null) box.kaariboga.handleMessage (message);
                 
                // deliver message to message handlers, that are not agents
                KbMessageHandler msgHandler = (KbMessageHandler) messageHandlers.get( message.recipient.name );
                if (msgHandler != null) msgHandler.handleMessage( message );
            }
            // dispatch message to registered message handlers
            Collection handlers = (Collection) messageTypeHandlers.get( message.kind );
            if (handlers != null){
		Log.write( Log.INFORMATION, "KaaribogaBase.handleMessage: Dispatching message " + message.kind + " to message handlers." );
                Iterator it = handlers.iterator();
                while ( it.hasNext() ){
                    ((KbMessageHandler) it.next()).handleMessage( message );
                }
            }
        }
        catch( IOException e ){
            Log.write( Log.ERROR, "KaaribogaBase.handleMessage: IOException at receiving an object! " + e );
        }
        catch( ClassNotFoundException e ){
            Log.write( Log.ERROR, "KaaribogaBase.handleMessage: ClassNotFoundException at receiving an object! " + e );
        }
    }


    /**
     * Dispatches a kaariboga to another base.
     * It stops the kaariboga, spawns a new thread to send it to another
     * base and removes it from the current base.
     *
     * @param kaariboga The agent that has to be dispatched.
     * @param address Address of the destination.
     */
    protected void dispatch (Kaariboga kaariboga, KaaribogaAddress address){
        //Thread thread;
        KaaribogaMessage msg;
        kaariboga.onDispatch();
        KaaribogaBox box = (KaaribogaBox) boxes.get(kaariboga.getName());
        if (box.thread.isAlive()) box.thread = null;

        // wrap kaariboga into a new message and send it.
        try {
            KaaribogaAddress msgSender = new KaaribogaAddress( baseAddress.host,
                                                               port, kaariboga.getName() );
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            KaaribogaOutputStream mos = new KaaribogaOutputStream(bos);
            mos.writeObject (kaariboga);

            msg = new KaaribogaMessage(msgSender, address, "KAARIBOGA", "", bos.toByteArray());
	    deliveryService.postMessage( msg );
        }
        catch (UnknownHostException e){
            System.err.println ("! KaaribogaBase.dispatchRequest: " + e);
        }
        catch (IOException e){
            System.err.println ("! KaaribogaBase.dispatchRequest: " + e );
        }

        kaariboga.onDestroy();
        fireKaaribogaLeft (kaariboga.getName());
        boxes.remove (kaariboga.getName()); // oo kaariboga is killed if dispatch fails
        classManager.dec(kaariboga.getName());
    }


    /**
     * Shuts down the network connection.
     */
    public void dispose(){
        if (listenThread != null) goOffline(parent);
    }

    /**
     * Adds a kaariboga to the base and starts it's thread.
     * This method should be called to add an arriving Kaariboga
     * agent to the base.
     */
    public synchronized void addKaaribogaOnArrival(Kaariboga kaariboga, InetAddress sender){
        kaariboga.setBase(this);
        kaariboga.addKaaribogaListener(this);
        kaariboga.addKaaribogaMessageListener(this);
        Thread thread = new Thread (kaariboga);
        java.util.Date time = new java.util.Date();
        KaaribogaBox box = new KaaribogaBox(kaariboga, thread, time, sender);
        boxes.put(kaariboga.getName(), box);
        kaariboga.onArrival();
        thread.start();
        fireKaaribogaArrived (kaariboga.getName());
    }

    /**
     * Adds a kaariboga to the base, performs some initialisation and
     * starts it's thread.
     * This method should be called to add a newly created
     * Kaariboga agent to the base.
     */
    public synchronized void addKaaribogaOnCreation(Kaariboga kaariboga, InetAddress sender){
        kaariboga.setBase(this);
        kaariboga.addKaaribogaListener(this);
        kaariboga.addKaaribogaMessageListener(this);
        Thread thread = new Thread (kaariboga);
        java.util.Date time = new java.util.Date();
        KaaribogaBox box = new KaaribogaBox(kaariboga, thread, time, sender);
        boxes.put(kaariboga.getName(), box);
        kaariboga.onCreate();
        thread.start();
        fireKaaribogaCreated (kaariboga.getName());
    }

    /**
     * Handles the dispatch request of a kaariboga.
     */
    public void kaaribogaDispatchRequest(KaaribogaEvent e){
        Kaariboga kaariboga = (Kaariboga) e.getSource();
        KaaribogaAddress destination = kaariboga.getDestination();
        System.out.println ("Destination: " + destination.host.toString());
        dispatch (kaariboga, destination);
    }

    /**
     * Handles the sleep request of a kaariboga.
     */
    public void kaaribogaSleepRequest(KaaribogaEvent e){
        Kaariboga kaariboga = (Kaariboga) e.getSource();
        kaariboga.onSleep();
    }

    /**
     * Handles the destroy request of a kaariboga.
     */
    public void kaaribogaDestroyRequest(KaaribogaEvent e){
        Kaariboga kaariboga = (Kaariboga) e.getSource();
        destroyKaariboga(this, kaariboga.getName());
    }

    /**
     * Gets the names of all kaariboga agents on this base.
     *
     * @param sender The object that sends the request.
     */
    public Enumeration getKaaribogaNames(Object sender){
        return boxes.keys();
    }

    /**
     * Generates a unique name that can be used to
     * initialize agents.
     */
    public String generateName(){
        String localHost;
        try{
         // XXX this does not work correctly if the host has no 
         // IP address, can happen in case of dynamic IP addresses
            localHost = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e){
            System.err.println ("! KaaribogaBase.generateName: Base could not determine local host!" + e);
            localHost = "Unknown Host";
        }
        return ++counter + " " + localHost + " " + new Date().toString();
    }

    /**
     * Returns the KaaribogaAddress of this base.
     *
     * @param sender The object that requests the address.
     */
    public KaaribogaAddress getBaseAddress (Object sender){
        return baseAddress;
    }

    /**
     *  If the server has no static IP address this
     *  method is called to determine the dynamic
     *  IP address.
     *
     *  @param sender The object that calls this method
     */
    public void goOnlineForDynamicIP( Object sender ) 
    throws UnknownHostException, 
           IOException,
           InterruptedException
    {        
        baseAddress = null;
            
        // ask ipServer for own IP address
        KaaribogaAddress localBase = new KaaribogaAddress (null, port, null);
        KaaribogaMessage msg = new KaaribogaMessage (localBase,
                                                     ipServer,
                                                     "GET_MY_IP_ADDRESS",
                                                     null,
                                                     null);
	deliveryService.goOnline();
	deliveryService.postMessage( msg );

        // wait until the correct base address in known
        int timeout = 20000;
        int sleeptime = 500;
        int time = 0;
        while (baseAddress == null) {
            try { Thread.sleep (sleeptime); }
            catch (InterruptedException e) {} 
            time = time + sleeptime;
            if (time >= timeout) {
                deliveryService.goOffline();
                throw new UnknownHostException ("Could not reach IP address server.");
            }
        }        
    }
    
    
    /**
     *  Performs all neccessary actions to go online
     *
     *  @param sender The object that calls this method.
     *                Only the parent of this class may call this method,
     *                agents are not allowed to do this.
     *  @param portNo The port on which the server should listen
     *  @param boServer The domain server, if there is one. May be null.
     *  @param ipServer The server that determines this host's IP address
     *                  if dynamic IP addresses are used
     *   XXX port is ignored at the moment.  It is determined at server start up
     *       
     */
    public void goOnline (Object sender,
                          int portNo,
                          KaaribogaAddress boServer,
                          KaaribogaAddress ipServer) {
        if (sender != parent) return;
        if (isOnline) return;

        this.port = portNo;
        this.boServer = boServer;
        this.ipServer = ipServer;

        try{
            if (ipServer != null) goOnlineForDynamicIP (this);

	    baseAddress.port = port;
            deliveryService.goOnline();
            isOnline = true;
            System.out.println ("Server is online: " + baseAddress);
        }
        catch (UnknownHostException e){
            System.err.println ("! KaaribogaBase.goOnline: " + e);
            System.exit(1);
        }
        catch (IOException e){
            System.err.println ("! KaaribogaBase.goOnline: " + e);
            System.exit(1);
        }
        catch (InterruptedException e){
            System.err.println ("! KaaribogaBase.goOnline: listenThread for dynamic IP addresses can't be terminated. " + e);
            System.exit(1);
        }

        // Notify domain server.
        if (boServer != null){
            KaaribogaMessage msg = new KaaribogaMessage (baseAddress,
                                                         boServer,
                                                         "BASE_ONLINE",
                                                         null,
                                                         null);
	    deliveryService.postMessage( msg );
        }
    }

    /**
     *  Performs all neccessary actions to go offline
     *
     *  @param sender The object that calls this method. 
     *                Only the parent of this class may call this method, 
     *                agents are not allowed to do this.
     *  XXX This method performs no check for the threads that are still running
     */
    public void goOffline (Object sender){
        if (sender != parent) return;
        if (isOnline == false) return;
        
        // Notify Base online server.
        if (boServer != null){
            KaaribogaMessage msg = new KaaribogaMessage (baseAddress,
                                                         boServer,
                                                         "BASE_OFFLINE",
                                                         null,
                                                         null);
            deliveryService.postMessage( msg );
        }

        // in case of dynamic IP addressing
        if (ipServer != null) baseAddress = null;

        deliveryService.goOffline();
        isOnline = false;
    }

    /**
     *  Determines if the server is online.
     *  An agent could check the online state before it tries to travel
     *  to another server.
     */
    public boolean getOnlineStatus() {
        return isOnline;
    }

    /**
     * Destroys a kaariboga agent with all necessary clean up.
     *
     * @param sender Object that calls this method.
     * @param name Name of the kaariboga agent.
     */
    public void destroyKaariboga (Object sender, String name){
        KaaribogaBox box = (KaaribogaBox) boxes.get(name);
        if (box != null){
            box.kaariboga.onDestroy();
            fireKaaribogaDestroyed (box.kaariboga.getName());
            boxes.remove(name);
        }
    }

    /**
     * Dispatches a kaariboga agent with all necessary clean up.
     *
     * @param sender Object that calls this method.
     * @param name Name of the kaariboga agent.
     * @param destination Where to send the kaariboga.
     */
    public void dispatchKaariboga (Object sender, String name, KaaribogaAddress destination){
        KaaribogaBox box = (KaaribogaBox) boxes.get(name);
        if (box != null){
            System.out.println ("Destination: " + destination.host.toString());
            dispatch (box.kaariboga, destination);
        }
    }


    /**
     * Adds a base event listener.
     */
    public synchronized void addBaseListener (BaseListener l){
        if (baseListeners.contains(l)) return;

        baseListeners.addElement (l);
    }

    /**
     * Removes a base event listener.
     */
    public synchronized void removeBaseListener (BaseListener l){
        baseListeners.removeElement (l);
    }

    /**
     *  Fires an event if a new kaariboga was created.
     *
     * @param name Name of the kaariboga agent.
     */
    protected void fireKaaribogaCreated (String name){
        Vector listeners;
        synchronized (this){
            listeners = (Vector) baseListeners.clone();
        }
        int size = listeners.size();

        if (size == 0) return;

        BaseEvent e = new BaseEvent (this, name);
        for (int i = 0; i < size; ++i) {
            ( (BaseListener) baseListeners.elementAt(i) ).baseKaaribogaCreated(e);
        }
    }

    /**
     * Fires an event if a new kaariboga has arrived.
     *
     * @param name Name of the kaariboga agent.
     */
    protected void fireKaaribogaArrived (String name){
        Vector listeners;
        synchronized (this){
            listeners = (Vector) baseListeners.clone();
        }
        int size = listeners.size();

        if (size == 0) return;

        BaseEvent e = new BaseEvent (this, name);
        for (int i = 0; i < size; ++i) {
            ( (BaseListener) baseListeners.elementAt(i) ).baseKaaribogaArrived(e);
        }
    }

    /**
     * Fires an event if a kaariboga has been destroyed.
     *
     * @param name Name of the kaariboga agent.
     */
    protected void fireKaaribogaDestroyed (String name){
        Vector listeners;
        synchronized (this){
            listeners = (Vector) baseListeners.clone();
        }
        int size = listeners.size();

        if (size == 0) return;

        BaseEvent e = new BaseEvent (this, name);
        for (int i = 0; i < size; ++i) {
            ( (BaseListener) baseListeners.elementAt(i) ).baseKaaribogaDestroyed(e);
        }
    }

    /**
     * Fires an event if a kaariboga was dispatched to another base.
     *
     * @param name Name of the kaariboga agent.
     */
    protected void fireKaaribogaLeft (String name){
        Vector listeners;
        synchronized (this){
            listeners = (Vector) baseListeners.clone();
        }
        int size = listeners.size();

        if (size == 0) return;

        BaseEvent e = new BaseEvent (this, name);
        for (int i = 0; i < size; ++i) {
            ( (BaseListener) baseListeners.elementAt(i) ).baseKaaribogaLeft(e);
        }
    }

    /**
     * Handles messages that are fired by kaaribogas.
     */
    public void kaaribogaMessage (KaaribogaMessageEvent e){
        KaaribogaMessage message = e.getMessage();
        Kaariboga receiver;

        if (message == null || message.recipient == null) return;

        if (message.recipient.host == null)
	{ // localhost
		KaaribogaBox box = null;
	        if (message.recipient.name != null) 
			box = (KaaribogaBox) boxes.get(message.recipient.name);
		else
		{
			handleMessage(message);
			return;
		}
		
		if( box != null )
		{
			receiver = (Kaariboga) box.kaariboga;
			if (receiver != null) receiver.handleMessage (message); 
			//XXX Exception if Hashtable.get fails?
		}
		else System.err.println("No local agent " +
						message.recipient.name);
	}
	deliveryService.postMessage( message );
    } // kaariboga Message


    /**
     *  Adds a message handler, that is not an agent.
     *  @param handler The message handler
     *  @param id A unique id of the message handler
     */
    public synchronized void addMessageHandler( KbMessageHandler handler, String id ){
        messageHandlers.put( id, handler );
    }


    /**
     *  Removes a message handler, that is not an agent.
     *  @param handler The message handler.
     *                 This parameter is only for security reasons. Otherwise any
     *                 agent could remove another message handler.
     *  @param id A unique id of the message handler
     */
    public synchronized void removeMessageHandler(  KbMessageHandler handler, String id ){
        if ( messageHandlers.get( id ) == handler ) messageHandlers.remove( id );
    }


    /**
     *  Adds a message handler for a specified kind of message to this base.
     *  The ability of a base to handle messages may be extended by different message handlers.
     *  There may be more than one message handler for one kind of message.
     *
     *  @param handler The message handler, that wants to receive the specified kind of message
     *  @param kind The kind of message, that should be delivered to the message handler
     *  @return True, if successfull
     *  @see org.kaariboga.core.KaaribogaMessage
     */
    public synchronized boolean addMessageTypeHandler( KbMessageHandler handler, String kind ){
        Collection handlers = (Collection) messageTypeHandlers.get( kind );
        if (handlers == null){
            // no handle for this type of message
            handlers = new LinkedList();
            handlers.add( handler );
            messageTypeHandlers.put( kind, handlers );
            return true;
        }
        else {
            // there is at least one handler for this type of message
            handlers.add( handler );
            return true;
        }
    }


    /**
     *  Removes a message handler for a specified kind of message to this base.
     *  The message handler must be the same as the one that registered for this kind of message.
     *
     *  @param handler The message handler, that wants to receive the specified kind of message
     *  @param kind The kind of message, that should be delivered to the message handler
     *  @return True if successfull
     *  @see org.kaariboga.core.KaaribogaMessage
     */
    public synchronized boolean removeMessageTypeHandler( KbMessageHandler handler, String kind ){
        Collection handlers = (Collection) messageTypeHandlers.get( kind );
        if ( handlers != null ){
            return handlers.remove( handler );
        }
        else return false;
    }


    /**
     *  Adds a service to this base.
     *  A plug-in may provide different services.
     *  Agents may use the service name to get access to a service object.
     *
     *  @param plugIn The plugIn that provides the service
     *  @param name Name of the service
     */
    public synchronized boolean addService( KbPlugIn plugIn, String name ){
        KbPlugIn oldPlugIn = (KbPlugIn) services.get( name );
        if (oldPlugIn == null){
            services.put( name, plugIn );
            return true;
        }
        else return false;
    }


    /**
     *  Removes the service named name from this base.
     +  The service can only be removed by the plug-in that 
     *  provided it.
     *  A plug-in may provide more than one services.
     *  So this method does not neccessary remove the plug-in.
     *
     *  @param plugIn The plugIn that provides the service
     *  @param name Name of the service
     */
    public synchronized boolean removeService( KbPlugIn plugIn, String name ){
        KbPlugIn oldPlugIn = (KbPlugIn) services.get( name );
        if (oldPlugIn == plugIn){
            services.remove( name );
            return true;
        }
        else return false;
    }


    /**
     *  Returns the names of all the services currently available.
     *  A service is provides by a plug-in.
     * 
     *  @param sender The object that calls this method.
     *  @return Names of the available services.
     */
    public synchronized String getServiceNames( Object sender )[] {
        Collection names = services.keySet();
        String[] result = (String[]) names.toArray( new String[0] );
        return result;
    }


    /**
     *  Returns a service object for a specified service name.
     *  The caller of this method must make shure, that the
     *  returned object is of the expected type.
     *
     *  @param sender The object that calls this method.
     *  @param name Name of the requested service
     *  @return An object that implements the requested Service,
     *          null if the object is not available or the sender
     *          is not allowed to request the object.
     */
    public Object getServiceObject( Object sender, String name ){
        KbPlugIn plugIn = (KbPlugIn) services.get( name );
        if ( plugIn != null )
            return plugIn.getService( name );
        else
            return null;
    }
        
    /**
     *  Called in case a message could not be delivered.
     *  It is not guaranteed, that this method is called in every case, for example
     *  if a DeliveryService is offline, it may decide to store incoming messages
     *  until it is online again.
     *
     *  @param messageId Id of the message, that could not be delivered
     *  @param errorCode Delivery error code. At the moment the following codes exit:
     *                   BASE_NOT_REACHABLE, AGENT_NOT_REACHABLE, BASE_OFFLINE
     */
    public void messageDeliveryError( long messageId, int errorCode ){
    }
    
    
    /**
     *  Loads a kaariboga agent
     *
     *  @param sender Object that calls this method
     *  @param name classname of the agent,
     *         for example org.kaariboga.agents.HelloAgent
     */
    public void loadKaariboga( Object sender, String name )
    throws InvocationTargetException,
	   SecurityException,
           ClassNotFoundException,
           IllegalAccessException,
           InstantiationException
    {
        Class result;
        KaaribogaClassLoader loader;
        loader = new KaaribogaClassLoader( classManager,
                                           this,
	                	           baseAddress,
                                           timeout,
                                           deliveryService );
        result = loader.loadClass(name);
        if (result == null){
            Log.write( Log.ERROR, "KaaribogaBase.loadKaariboga: Could not load class! Class not found!");
            return;
        }
        Constructor cons[] = result.getConstructors();
        Object obs[] = { generateName() };
        Kaariboga agent = (Kaariboga) cons[0].newInstance(obs);
        addKaaribogaOnCreation( agent, null );
    }


}









