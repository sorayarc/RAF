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

import raf.agentes.*;
import raf.principal.*;


/**
 * Esta clase implementa un lanzador de entorno RAF con interfaz de usuario.
 */
public class GRaLauncher extends JFrame implements ActionListener,
                                            ListSelectionListener,
                                            AgencyListener{
    /**
     * Donde esta la configuracion del servidor.
     */
    String strConfigFile =  "raf" 
                          + File.separator
                          + "config"
                          + File.separator
                          + "movil.config";

    JFrame frame2;
    JMenuBar menuBar;
    JMenu menu;
    JMenuItem menuItem;
    ImageIcon icon = new ImageIcon("images/middle.gif");
    //JFileChooser fileChooser;
    JPanel panel = new JPanel();

    JList list;
    DefaultListModel listModel;
    JScrollPane listScroller;

    /**
     * Maneja los byte codes de las clases cargadas.
     */
    ClassManager classManager;

    /**
     * Nombre del agente que fue seleccionado en la lista.
     */
    String selectedRa = null;

	/**
	 * La agencia que maneja todos los agentes.
	 */
	private RaAgency raAgency;

    /**
     * Direccion del servidor que registra todos los servidores de agentes del dominio.
     */
    RaAddress raServer;

    /**
     * Nº puerto para las conexiones, por defecto 10101.
     */
    int port;

    /**
     * Crea un nuevo lanzador GRaLauncher.

     */
    public GRaLauncher (){
        super("GRaLauncher");

        long byteCodeDelay;
        String strRaServer = null;
        int raPort;
        Properties props = new Properties ();

        // lee las propiedades desde el fichero
        try {
            FileInputStream in = new FileInputStream (strConfigFile);
            props.load (in);
            in.close();
        }
        catch (FileNotFoundException e){
            System.err.println ("GraLauncher: No se puede abrir el fichero de configuración!");
        }
        catch (IOException e){
            System.err.println ("GRaLauncher: Ha fallado la lectura del fichero!");
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
            System.out.println("! GRaLauncher: raServer no valido." + e);
            raServer = null;
        }

        System.out.println ("puerto: " + port);

        // lanza una nuva agencia
        classManager = new ClassManager (byteCodeDelay, props.getProperty("agentsPath"));

        raAgency = new RaAgency (this, classManager);
        raAgency.addAgencyListener (this);
	setVisible(false);

       	// crea el menu principal
       	menuBar = new JMenuBar();
       	setJMenuBar (menuBar);

       	// menu fichero
       	menu = new JMenu ("Fichero");
       	menuItem = new JMenuItem ("Cargar...");
       	menuItem.setActionCommand ("Cargar...");
       	menuItem.addActionListener (this);
       	menu.add (menuItem);
       	menuBar.add (menu);

       	// Editar menu
       	menu = new JMenu ("Editar");
       	menuItem = new JMenuItem ("Enviar A...");
       	menuItem.setActionCommand ("editSendTo");
       	menuItem.addActionListener (this);
       	menu.add (menuItem);
       	menuItem = new JMenuItem ("Eliminar");
       	menuItem.setActionCommand ("editDestroy");
       	menuItem.addActionListener (this);
       	menu.add (menuItem);
       	menuBar.add (menu);

      

      
        // contenidos del Frame 
        getContentPane().add (panel);
        panel.setLayout (new GridLayout(1,1));
        panel.setPreferredSize(new java.awt.Dimension(500, 300));

        listModel = new DefaultListModel();
        list = new JList (listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        listScroller = new JScrollPane (list);

        panel.add (listScroller);

        startAgency();
    }


    /**
     *  Llamada para el limpiado de las conexiones de red.
     */
    public void dispose() {
        stopAgency();
    }


    /**
     * Crea un nuevo servidor GraLauncher
     */
    public static void main(String[] args){

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (Exception e) {
            System.err.println("No se ha podido establecer el look and feel multiplataforma: " + e);
        }

        JFrame frame = new GRaLauncher();
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ((GRaLauncher) e.getWindow()).dispose();
                System.exit(0);
            }
        };
        frame.addWindowListener(l);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Maneja los eventos de menu
     */
    public void actionPerformed (ActionEvent e){
        if ( e.getActionCommand().equals ("Cargar...") ) {
           
	    /*
            File agentsPath = new File ( File.separator + "raf" + File.separator + "agentes" + File.separator + "*.class" );
         //   cargar los nombred de los agentes
           String[] lista = agentsPath.list();
           //System.out.println (lista);
           Object[] lis = (Object[]) agentsPath.list();
           String s = (String) JOptionPane.showInputDialog(
                     frame2,
                     "Elige un Agente",
                     "Agentes Moviles",
                     JOptionPane.PLAIN_MESSAGE,
		     icon,
                     lis,
                     lis[0]);
                     if (s != null) {
                         s = s.trim();
                         if (s.length() >0 ) {
                             loadRa ("s);
                         }
                     }*/
		     loadRa("HelloDomain");
  
        }
        if ( e.getActionCommand().equals ("editSendTo") ) {
         int i;
              i=0; 
                 Object[] v = new Object[50];
                 Enumeration enum = raAgency.getServers(this).elements();
                 while (enum.hasMoreElements()){
                 v[i] = (Object) enum.nextElement();
                   i = i + 1;
                 }
		String s = (String) JOptionPane.showInputDialog(
                     frame2,
                     "Elige una Agencia",
                     "Agencia Destino",
                     JOptionPane.PLAIN_MESSAGE,
		     icon,
		     v,
                     v[0]);
                     if (s != null) {
                         s = s.trim();
                         if (s.length() >0 ) {
                           System.out.println (s);
                             editSendTo (s);
                             }
                     }
     
            editSendTo(s);
        }
        if ( e.getActionCommand().equals ("editDestroy") ) {
            editDestroy();
        }
       

    }

    /**
     * carga un agente desde un fichero (poner un string como parametro)
     */
    public void loadRa(String s){
      
        String nombre;

        nombre = "raf.agentes." + s;
        try{
            Class result;
            RaClassLoader loader = new RaClassLoader(classManager, null, null);
            result = loader.loadClass(nombre);
            if (result == null){
                System.err.println ("GRaLauncher: No se pudo cargar la clase! clase no encontrada!");
                return;
            }

            Constructor cons[] = result.getConstructors();
            Object obs[] = {raAgency.generateName()};
	        Ra agent = (Ra) cons[0].newInstance(obs);
            raAgency.addRaOnCreation (agent, null);
        }
        catch (InvocationTargetException e){
            System.err.println ("! GRaLauncher: No se ha podidio cargar la clase " + e);
        }
        catch (SecurityException e){
            System.err.println ("! GRaLauncher: No se ha podido cargar la clase! " + e);
        }
        catch (ClassNotFoundException e){
            System.err.println ("! GRaLauncher: No se ha podido cargar la clase!  " + e);
        }
        catch (IllegalAccessException e){
            System.err.println ("! GRaLauncher: No se ha podido cargar la clase! " + e);
        }
        catch (InstantiationException e){
            System.err.println ("! GRaLauncher: No se ha podido cargar la clase! " + e);
        }
    }

    /**
     * Envia el agente seleccionado a otra agencia.
     */
    void editSendTo(String s){
        InetAddress destination = null;
        String server = null;
        String servername = null;
        String strLoPort = null;
        int loPort = port;
        
        server = s;

        try{
            
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
            raAgency.dispatchRa (this, selectedRa, new RaAddress (destination, loPort, null));
        }
        catch (IndexOutOfBoundsException e){
            System.err.println("! GRaLauncher.editSendTo: Formato erroneo del puerto " + e);
        }
        catch (NumberFormatException e){
            System.err.println("! GRaLauncher.editSendTo: Formato erroneo del puerto " + e);
        }
        catch (UnknownHostException e){
            System.err.println("! GRaLauncher.editSendTo: No se puede determinar la dirccion del host " + e);
        }
    }


    /**
     * Borra el agente seleccionado.
     */
    void editDestroy(){
        raAgency.destroyRa (this, selectedRa);
    }

    /**
     * Inicializa el Thread del RaAgency.
     */
    void startAgency(){
	System.out.println ("Inicializando la Agencia");
        raAgency.startAgency (this, port, raServer);
    }

    /**
     * Para el Thread de la agencia.
     */
    void stopAgency(){
	System.out.println ("Parando la Agencia");
        raAgency.stopAgency (this);
    }

   
    /**
     * Recuerda el agente que se selecciono de la lista.
     */
    public synchronized void valueChanged(ListSelectionEvent e){
        int pos = list.getSelectedIndex();
        selectedRa = (String) listModel.elementAt (pos);
        System.out.println ("seleccionado: " + selectedRa);
    }

    /**
     * Reaccion cuando se ha puesto a un agente en el estado onCreate

     */
    public void agencyRaCreated (AgencyEvent e){
        listModel.addElement (e.getName());
    }

    /**
     * Reaccion cuando se ha puesto a un agente en el estado onArrival
     

     */
    public void agencyRaArrived (AgencyEvent e){
        listModel.addElement (e.getName());
    }

    /**
     * Reaccion cuando un agente abandona la agencia.
     */
    public void agencyRaLeft (AgencyEvent e){
        listModel.removeElement ((String)e.getName());
    }

    /**
     * Reaccion cuando se ha borrado un agente.
     */
    public void agencyRaDestroyed (AgencyEvent e){
        listModel.removeElement ((String)e.getName());
    }


}

