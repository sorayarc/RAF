package raf.raservidor;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Hashtable;

import raf.principal.*;

/**
 * Una clase que construye dominios administrativos.
 * Cada agencia se debe registrar aqui con un mensaje AGENCY_ONLINE.
 * Si una agencia deja de estar en linea debe enviar un mensaje AGENCY_OFFLINE
 * Si ocurre algun cambio en el dominio se les notifica a las agencias con un mensaje AGENCYS.
 */
public class RaModel
{
    /**
     * Gestiona los mensajes que vienen a través de una conexión socket.
     * Los Mensajes pueden ser AGENCY_ONLINE o AGENCY_OFFLINE.
     */
    class ReceiveMessageThread extends Thread{
        private Socket socket;
        private RaModel raModel;
        private RaMessage message;
        private RaMessage outMessage;
        private InetAddress address;

        /**
         * Crea un nuevo thread para recibir el mensaje.
         *
         * @param b El RaModel de este thread.
         * @param socket Socket de la conexion entrante.
         */
        public ReceiveMessageThread(RaModel b, Socket socket){
            this.socket = socket;
            this.raModel = b;
        }

        /**
         * Maneja el mensaje de entrada.
         */
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
                System.err.println("ReceiveMessageThread: IOException en los  streams de conexion al socket!");
            }

            try{
                message = (RaMessage) inStream.readObject();
                if ( !message.recipient.host.equals(raAddress.host) ){
                    // se reenvia el mensaje
                    new SendMessageThread(message).start();
                }
                else
                if ( message.kind.equals("AGENCY_ONLINE") ){
                    System.out.println ("ReceiveMessageThread: Ha llegado un mensaje AGENCY_ONLINE: " + message.sender.host.toString());
                    int puerto = message.sender.port; 
                    agencys.put ((message.sender.host.toString() + ":" + Integer.toString (puerto)), message.sender);
                    raModel.broadcast();
                }
                else if ( message.kind.equals("AGENCY_OFFLINE") ){
                    System.out.println ("ReceiveMessageThread: Ha llegado un Mensaje AGENCY_OFFLINE: " + message.sender.host.toString() );
                    agencys.remove (message.sender.host.toString());
                    raModel.broadcast();
                }
            }
            catch (IOException e){
                System.err.println("ReceiveMessageThread: IOException en la transferencia de datos!");
            }
            catch (ClassNotFoundException e){
                System.err.println ("ReceiveMessageThread: ClassNotFoundException al recibir el objeto!");
            }

            try{
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
                if (socket != null) socket.close();
            }
            catch (IOException e){
                // no deberia suceder nunca
                System.err.println("ReceiveMessageThread: IOException en el limpiado!");
            }
        }
    } // ReceiveMessageThread


    /**
     * Escucha en el puerto especificado y lanza nuevos threads para recibir
     * mensajes de entrada.
     */
    class ListenThread extends Thread
    {
        private RaModel parent;
        private Socket socket = null;

        public ListenThread (RaModel parent){
            this.parent = parent;
        }

        public void run(){
            Thread shouldLive = listenThread;
            try{
                while (shouldLive == listenThread){
                    socket = serverSocket.accept();
                    System.out.println ("ListenThread: Recibiendo un mensaje");
                    new ReceiveMessageThread (parent, socket).start();
                    yield();
                }
            }
            catch (IOException e){
                System.err.println("ListenThread: Exception al aceptar el mensaje!");
                System.exit(1);
            }
        }
    } // Listen Thread


    /**
     * Envia un RaMessage a otra agencia.
     */
    class SendMessageThread extends Thread{
        RaMessage msg;

        /**
         * Crea un thread que envia un mensaje a otro host.
         * El mensaje debe contener la dirección destino!
         *
         * @param msg El mensaje a enviar.
         */
        public SendMessageThread(RaMessage msg){
            this.msg = msg;
        }

        /**
         * Envia el mensaje a través deuna conexión de socket.
         */
        public void run(){
            Socket socket = null;
            ObjectOutputStream outStream = null;
            ObjectInputStream inStream = null;

            try {
                socket = new Socket(msg.recipient.host, msg.recipient.port);
                System.out.println ("SendMessageThread: socket creado a: " + msg.recipient.host.toString() + " " + msg.recipient.port);
                outStream = new ObjectOutputStream(
                                    socket.getOutputStream());
                inStream = new ObjectInputStream(
                                new BufferedInputStream(
                                    socket.getInputStream()));
                outStream.writeObject (msg);
                outStream.flush();
                System.out.println ("SendMessageThread: Escrito mensaje en el socket.");
            }
            catch (IOException e){
                System.err.println("SendMessageThread: IOException al enviar!");
            }

            try{
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
                if (socket != null) socket.close();
            }
            catch (IOException e){
                // no deberia suceder nunca
                System.err.println("SendMessageThread: IOException en el limpiado!");
            }
        }
    } // SendMessageThread


    /**
     * Todas las agencias conectadas en el dominio
     */
    Hashtable agencys;

    /**
     * Direccion de este servidor.
     */
    RaAddress raAddress;

    /**
     * Puerto en el que escucha el servidor.
     */
    int port;

    /**
     * Socket en el puerto principal.
     */
    ServerSocket serverSocket = null;

    /**
     * Thread que acepta conexiones de red.
     */
    volatile Thread listenThread = null;

    /**
     * Crea un nuevo servidor que maneja el estado del dominio.
     */
    public RaModel(){
        agencys = new Hashtable();
    }

    /**
     * Baja la conexion de red.
     */
    public void dispose(){
        try{
             listenThread = null;
             serverSocket.close();
             System.out.println ("Server socket cerrado");
        }
        catch (IOException e){
            System.err.println ("No se ha podido cerrar el ServerSocket!");
            System.exit(1);
        }
    }

    /**
     * Notifica a todas las agencias conectadas al dominio qué otras agencias
     * estan en linea en el dominio
     */
     
     public void broadcast(){
        RaMessage message = null;
        Hashtable servers = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;

        try{
            synchronized (this){
                servers = (Hashtable) agencys.clone();
            }
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream (bos);
            oos.writeObject (servers);

            // envia un mensaje AGENCYS a todos los servidores conectados.
            for (Enumeration e = servers.elements(); e.hasMoreElements();){
                message = new RaMessage (raAddress,
                                            (RaAddress)e.nextElement(),
                                            "AGENCYS",
                                            "",
                                            bos.toByteArray());
                new SendMessageThread (message).start();
            }
        }
        catch (IOException e){
            System.err.println ("BoModel: Broadcast ha fallado!");
        }
    }

    /**
     * Comienza ha escuchar esperando mensajes.
     */
    public void startService (int portNo){
        port = portNo;
        try{
             raAddress = new RaAddress(InetAddress.getLocalHost(), port, null);

             serverSocket = new ServerSocket(port);
             System.out.println ("Escuchando en el puerto: " + port);
        }
        catch (UnknownHostException e){
            System.err.println ("RaModel: No se ha podido determinar la direccion del  host local!");
            System.exit(1);
        }
        catch (IOException e){
            System.err.println ("RaModel: No se ha podido crear el ServerSocket!");
            System.exit(1);
        }
        listenThread = new ListenThread(this);
        listenThread.start();
    }

    /**
     * Deja de escuchar en el dominio de red.
     */
    public void stopService(){
        try{
             listenThread = null;
             serverSocket.close();
             serverSocket = null;
             System.out.println ("Server socket cerrado");
        }
        catch (IOException e){
            System.err.println ("RaModel: No se ha podido cerrar el ServerSocket!");
            System.exit(1);
        }
    }

}
