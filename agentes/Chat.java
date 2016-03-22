package raf.agentes;

import java.io.Serializable;
import java.lang.InterruptedException;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;


import raf.principal.Ra;
import raf.principal.RaAddress;
import raf.principal.RaEvent;


public class Chat extends Ra
{
    /**
     * List of all the servers in the domain.
     */
    Vector v;
    String s; /* cadena de conversacion
    public String t;
    

    /**
     * Points to the next destination in v.
     */
    int i;
    
  
    public Chat(String name){
        super("Chat_" + name);
    }

    
    public void onCreate(){
        i = 0;
        //s = ' ';
         s = "hola que tal";
        v = new Vector();
        RaAddress address;
        Enumeration enum = agency.getServers(this).elements();
        while (enum.hasMoreElements()){
            address = (RaAddress) enum.nextElement();
            v.addElement (address);
        }
    }

    /**
     * Shows a window.
     */
    public void onArrival(){
       //muestra la ventan de chat
      
        final TextComponent frame = new TextComponent();
        

        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
            public void windowActivated(WindowEvent e) {
		frame.textPane.requestFocus();
            }
        };
        frame.addWindowListener(l);

        frame.pack();
        frame.setVisible(true);

        
    }

    /**
     * This is automically called if the agent arrives on
     * a base.
     */
    public void run(){
       
        try{
            if (i < v.size()){
                destination = (RaAddress) v.elementAt(i);
                ++i;
                System.out.println("Intentando disparar");
                fireDispatchRequest();
            }
            else fireDestroyRequest();
        }
        catch (ArrayIndexOutOfBoundsException e){
            System.err.println ("Chat: Fuera de los limites!");
            fireDestroyRequest();
        }
    }

   public class TextComponent extends JFrame implements java.awt.event.ActionListener{
    JTextArea textPane;
    JTextArea changeLog;
    String newline;
    
    JButton aceptar;
    public TextComponent () {

	//Some initial setup
        super("Chatero");
	newline = System.getProperty("line.separator");

	//Create the text area and configure it
        //textPane = new JTextArea();
        textPane = new JTextArea(5, 40);
        
        textPane.setEditable(true);
	JScrollPane scrollPane = new JScrollPane(textPane);

	//Create the text area for the status log and configure it
        changeLog = new JTextArea(5, 30);
        changeLog.setText (s);
	changeLog.setEditable(false);
	JScrollPane scrollPaneForLog = new JScrollPane(changeLog);

	//Create a split pane for the change log and the text area
	JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					      scrollPane, scrollPaneForLog);
	splitPane.setOneTouchExpandable(true);
        aceptar = new JButton ("Aceptar");
        aceptar.addActionListener(this);
        aceptar.setActionCommand("enable");
	//Create the status area
	JPanel statusPane = new JPanel(new GridLayout(1, 1));
      
        //Add the components to the frame 
        BorderLayout borderLayout = new BorderLayout();
	JPanel contentPane = new JPanel();
        contentPane.setLayout(borderLayout);
        contentPane.add(splitPane, BorderLayout.NORTH);
        contentPane.add(statusPane, BorderLayout.CENTER);
        contentPane.add(aceptar,JButton.CENTER);
	setContentPane(contentPane);
        //a¤adir el boton  de aceptar que guarda el texto del textpane
    }

 public void actionPerformed (java.awt.event.ActionEvent e){
    if (aceptar.getActionCommand().equals("enable")){
        textPane.selectAll();
        s = s + textPane.getSelectedText();
      
        //cerrar ventana
       
        }
     }
                                                           
    
}


}
