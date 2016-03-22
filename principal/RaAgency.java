package raf.principal;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class RaAgency
    implements Serializable, RaListener, RaMessageListener
{
    /**
     * No usado.
     */
    public final int version = 0;

  
    private Object parent;

  
    private Hashtable agencys;

    

    private RaAddress raServer;

  
    private Vector agencyListeners;

   
    long delay = 100000;


    private int port;

    
    private RaAddress agencyAddress;

   
    private long counter = 0;

   
    private volatile Thread listenThread = null;

   
    protected ClassManager classManager;

   
    Hashtable boxes;

    
    ServerSocket serverSocket = null;

   
    class ReceiveMessageThread extends Thread implements Serializable{
        private Socket socket;
        private RaAgency agency;
        private RaMessage message;
        private RaMessage outMessage;
        private InetAddress address;
        private Ra agent;

       
        public ReceiveMessageThread(RaAgency b, Socket socket){
            this.socket = socket;
            this.agency = b;
        }

     
        public void run(){
            ObjectOutputStream outStream = null;
            ObjectInputStream inStream = null;

            try{
                inStream = new ObjectInputStream(
                                new BufferedInputStream(
                                    socket.getInputStream()));
                outStream = new ObjectOutputStream(
                                    socket.getOutputStream());
                address = socket.getInetAddress();
            }
            catch (IOException e){
                System.err.println("! ReceiveMessageThread.run: " + e);
            }

            try{
                message = (RaMessage) inStream.readObject();
                if ( !message.recipient.host.equals(agencyAddress.host) ){
                    new SendMessageThread(message).start();
                }
                else
                if ( message.kind.equals("RA") ){
                    System.out.println ("ReceiveMessageThread: ha llegado un mensage RA.");
                    ByteArrayInputStream bInStream = new ByteArrayInputStream(message.binary);
                    RaInputStream mis = new RaInputStream(classManager, agencyAddress, bInStream, message.sender);
                    agent = (Ra) mis.readObject();
                    //agent.onArrival();
                    addRaOnArrival(agent, address);
                }
                else if ( message.kind.equals("AGENCYS") ){
                    System.out.println ("ReceiveMessageThread: ha llegado un mensaje AGENCYS.");
                    ByteArrayInputStream bis = new ByteArrayInputStream(message.binary);
                    ObjectInputStream ois = new ObjectInputStream (bis);
                    synchronized (this){
                        agencys = (Hashtable) ois.readObject();
                    }
                }
                else if ( message.kind.equals("GET_CLASS") ){
                    System.out.println ("ReceiveMessageThread: Ha llegado un mensaje GET_CLASS: " + message.content + ".class");
                    byte[] source = classManager.getByteCode (message.content);
                    outMessage = new RaMessage(agencyAddress,
                                                      message.sender,
                                                      "CLASS",
                                                       message.content,
                                                       source);
                    outStream.writeObject(outMessage);
                    outStream.flush();
                }
                else if (message.kind.equals("GET")){
                       RaBox target = (RaBox) boxes.get(message.content);
                    if (target != null){
                        target.ra.onDispatch();
                        target.thread = null;

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(bos);
                        oos.writeObject (target.ra);

                        outMessage = new RaMessage(agencyAddress,
                                                      message.sender,
                                                      "RA",
                                                       message.content,
                                                       bos.toByteArray());
                        outStream.writeObject(outMessage);
                        outStream.flush();


                        boxes.remove(message.content);
                        classManager.dec(message.content);
                    }
                    else {
            
                        outMessage = new RaMessage(agencyAddress,
                                                       message.sender,
                                                      "ERROR",
                                                       "Agente no encontrado!",
                                                       null);
                        outStream.writeObject(outMessage);
                        outStream.flush();
                    }
                }
                else if (message.recipient.name!=null) {

                    System.out.println ("Intentando devolver un mensaje al agente local" + message.recipient.name);
                    RaBox box = (RaBox) boxes.get(message.recipient.name);
                    if (box != null){
                        box.ra.handleMessage (message);
                    }
                }
                else {
                    
                    agency.handleMessage(message);
                }
	    }
            catch (IOException e){
                System.err.println("ReceiveMessageThread: IOException en la transferencia de datos!");
                System.err.println (e.getMessage());
            }
            catch (ClassNotFoundException e){
                System.err.println ("ReceiveMessageThread: ClassNotFoundException al recibir el objeto!");
                System.err.println (e.getMessage());
            }

            try{
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
                if (socket != null) socket.close();
            }
            catch (IOException e){

                System.err.println("ReceiveMessageThread: IOException en el limpiado!");
                System.err.println (e.getMessage());
            }
        }
    } // ReceiveMessageThread


    class ListenThread extends Thread implements Serializable
    {
        private RaAgency parent;
        private Socket socket = null;

        public ListenThread (RaAgency parent){
            this.parent = parent;
        }

        public void run(){
            Thread shouldLive = listenThread;
            try{
                while (shouldLive == listenThread){
                    socket = serverSocket.accept();
                    if(shouldLive != listenThread) return;
		    System.out.println ("ListenThread: tomando un mensaje");
                    new ReceiveMessageThread (parent, socket).start();
                    yield();
                }
            }
            catch (IOException e){
                System.err.println("! ListenThread.run: " + e);
            }
        }
    } // Listen Thread


    protected class SendMessageThread extends Thread implements Serializable{
        RaMessage msg;

        
        public SendMessageThread(RaMessage msg){
            this.msg = msg;
        }

       
        public void run(){
            Socket socket = null;
            ObjectOutputStream outStream = null;

            try {
                socket = new Socket(msg.recipient.host, msg.recipient.port);
                System.out.println ("SendMessageThread: socket created to: " + msg.recipient.host + " " + msg.recipient.port);
                outStream = new ObjectOutputStream(
                                    socket.getOutputStream());
                outStream.writeObject (msg);
                outStream.flush();
                System.out.println ("SendMessageThread: Wrote message to socket.");
            }
            catch (IOException e){
                System.err.println("! SendMessageThread.run,1: " + e + ": " + msg.recipient);
            }

            try{
                try { sleep(10000); } catch(Exception e) {}; 
                if (outStream != null) outStream.close();
                if (socket != null) socket.close();
            }
            catch (IOException e){

                System.err.println("! SendMessageThread.run,2: " + e);
            }
        }
    } // SendMessageThread


 
    public RaAgency (Object parent, ClassManager clManager){
        this.parent = parent;
        agencyListeners = new Vector();
        boxes = new Hashtable();
        classManager = clManager;
    }

   
    public void handleMessage (RaMessage msg){
        System.out.println ("Message version: " + msg.version);
        System.out.println ("Message kind: " + msg.kind);
        System.out.println ("Message content: " + msg.content);
    }

  
    protected void dispatch (Ra ra, RaAddress address){
        //Thread thread;
        RaMessage msg;
        ra.onDispatch();
        RaBox box = (RaBox) boxes.get(ra.getName());
        if (box.thread.isAlive()) box.thread = null;

        try {
            RaAddress msgSender = new RaAddress(
                                         InetAddress.getLocalHost(),
                                         port, ra.getName());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            RaOutputStream mos = new RaOutputStream(bos);
            mos.writeObject (ra);

            msg = new RaMessage(msgSender, address, "RA", "", bos.toByteArray());
            new SendMessageThread (msg).start();
        }
        catch (UnknownHostException e){
            System.err.println ("! RaAgency.dispatchRequest: " + e);
        }
        catch (IOException e){
            System.err.println ("! RaAgency.dispatchRequest: " + e );
        }

        ra.onDestroy();
        fireRaLeft (ra.getName());
        boxes.remove (ra.getName());
        classManager.dec(ra.getName());
    }


    public void dispose(){
        if (listenThread != null) stopAgency(parent);
    }

    public synchronized void addRaOnArrival(Ra ra, InetAddress sender){
        ra.setAgency(this);
        ra.addRaListener(this);
        ra.addRaMessageListener(this);
        Thread thread = new Thread (ra);
        java.util.Date time = new java.util.Date();
        RaBox box = new RaBox(ra, thread, time, sender);
        boxes.put(ra.getName(), box);
        ra.onArrival();
        thread.start();
        fireRaArrived (ra.getName());
    }

    
    public synchronized void addRaOnCreation(Ra ra, InetAddress sender){
        ra.setAgency(this);
        ra.addRaListener(this);
        ra.addRaMessageListener(this);
        Thread thread = new Thread (ra);
        java.util.Date time = new java.util.Date();
        RaBox box = new RaBox(ra, thread, time, sender);
        boxes.put(ra.getName(), box);
        ra.onCreate();
        thread.start();
        fireRaCreated (ra.getName());
    }

    public void raDispatchRequest(RaEvent e){
        Ra ra = (Ra) e.getSource();
        RaAddress destination = ra.getDestination();
        System.out.println ("Destino: " + destination.host.toString());
        dispatch (ra, destination);
    }

    
    public void raSleepRequest(RaEvent e){
        Ra ra = (Ra) e.getSource();
        ra.onSleep();
    }

  
    public void raDestroyRequest(RaEvent e){
        Ra ra = (Ra) e.getSource();
        destroyRa(this, ra.getName());
    }

  
    public Enumeration getRaNames(Object sender){
        return boxes.keys();
    }

    public String generateName(){
        String localHost;
        try{
            localHost = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e){
            System.err.println ("! RaAgency.generateName: La agencia no puede determinar el host local!" + e);
            localHost = "Host desconocido";
        }
        return ++counter + " " + localHost + " " + new Date().toString();
    }

    
    public RaAddress getAgencyAddress(Object sender){
        try
        {
                agencyAddress.host = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e){
                System.err.println ("! RaAgency.getAgencyAddress :" + e );
                System.exit(1);
        }
        return agencyAddress;
    }

    

    public void startAgency(Object sender, int portNo, RaAddress raServer){
        if (sender != parent) return;

        port = portNo;
        this.raServer = raServer;
        try{
             agencyAddress = new RaAddress(InetAddress.getLocalHost(), port, null);

             serverSocket = new ServerSocket(port);
             System.out.println ("Escuchando en el puerto: " + port);
        }
        catch (UnknownHostException e){
            System.err.println ("No se ha podido determinar la direccion del host local!");
            System.exit(1);
        }
        catch (IOException e){
            System.err.println ("! No se ha podido crear el ServerSocket!" + e);
            System.exit(1);
        }
        listenThread = new ListenThread(this);
        listenThread.start();

        if (raServer != null){
            RaMessage msg = new RaMessage (agencyAddress,
                                                         raServer,
                                                         "AGENCY_ONLINE",
                                                         null,
                                                         null);
            new SendMessageThread (msg).start();
        }
    }

    public void stopAgency (Object sender){
        if (sender != parent) return;


        if (raServer != null){
            RaMessage msg = new RaMessage (agencyAddress,
                                                         raServer,
                                                         "AGENCY_OFFLINE",
                                                         null,
                                                         null);
            try {
                Thread thread = new SendMessageThread (msg);
                thread.start();
                thread.join();
            }
            catch (InterruptedException e){
                System.err.println ("! RaAgency: La desconexion del raServer ha fallado!" + e);
            }
        }

        listenThread = null;
        try{
             serverSocket.close();
             serverSocket = null;
             System.out.println ("Server socket cerrado");
        }
        catch (IOException e){
            System.err.println ("! No se puede cerrar el ServerSocket " + e);
	    System.exit(1);
        }
    }

    public void destroyRa (Object sender, String name){
        RaBox box = (RaBox) boxes.get(name);
        if (box != null){
            box.ra.onDestroy();
            fireRaDestroyed (box.ra.getName());
            boxes.remove(name);
        }
    }

    public void dispatchRa (Object sender, String name, RaAddress destination){
        RaBox box = (RaBox) boxes.get(name);
        if (box != null){
            System.out.println ("Destino: " + destination.host.toString());
            dispatch (box.ra, destination);
        }
    }

    public Hashtable getServers (Object sender){
        Hashtable result = null;
        synchronized (this){
            if (agencys != null) result = (Hashtable) agencys.clone();
        }
        return result;
    }

    public synchronized void addAgencyListener (AgencyListener l){
        if (agencyListeners.contains(l)) return;

        agencyListeners.addElement (l);
    }


    public synchronized void removeAgencyListener (AgencyListener l){
        agencyListeners.removeElement (l);
    }

   
    protected void fireRaCreated (String name){
        Vector listeners;
        synchronized (this){
            listeners = (Vector) agencyListeners.clone();
        }
        int size = listeners.size();

        if (size == 0) return;

        AgencyEvent e = new AgencyEvent (this, name);
        for (int i = 0; i < size; ++i) {
            ( (AgencyListener) agencyListeners.elementAt(i) ).agencyRaCreated(e);
        }
    }

   
    protected void fireRaArrived (String name){
        Vector listeners;
        synchronized (this){
            listeners = (Vector) agencyListeners.clone();
        }
        int size = listeners.size();

        if (size == 0) return;

        AgencyEvent e = new AgencyEvent (this, name);
        for (int i = 0; i < size; ++i) {
            ( (AgencyListener) agencyListeners.elementAt(i) ).agencyRaArrived(e);
        }
    }

   

    protected void fireRaDestroyed (String name){
        Vector listeners;
        synchronized (this){
            listeners = (Vector) agencyListeners.clone();
        }
        int size = listeners.size();

        if (size == 0) return;

        AgencyEvent e = new AgencyEvent (this, name);
        for (int i = 0; i < size; ++i) {
            ( (AgencyListener) agencyListeners.elementAt(i) ).agencyRaDestroyed(e);
        }
    }

   
    protected void fireRaLeft (String name){
        Vector listeners;
        synchronized (this){
            listeners = (Vector) agencyListeners.clone();
        }
        int size = listeners.size();

        if (size == 0) return;

        AgencyEvent e = new AgencyEvent (this, name);
        for (int i = 0; i < size; ++i) {
            ( (AgencyListener) agencyListeners.elementAt(i) ).agencyRaLeft(e);
        }
    }

    public void raMessage (RaMessageEvent e){
        RaMessage message = e.getMessage();
        Ra receiver;

        if (message != null)
	{
            if (message.recipient != null)
	    {
                if (message.recipient.host == null)
		{ // localhost
		    RaBox box = null;
	            if (message.recipient.name != null) 
		    	box = (RaBox) boxes.get (message.recipient.name); 
                    if( box != null )
		    {
		    	receiver = (Ra) box.ra;
                    	if (receiver != null) receiver.handleMessage (message); 
			
		    }
		    else System.err.println("No es un agente local " + message.recipient.name);
                }
                else {
                    new SendMessageThread(message).start();
                }
            }
        }
    } 

}
