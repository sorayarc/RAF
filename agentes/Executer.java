package org.kaariboga.agents;

import java.io.IOException;
import java.lang.Runtime;
import org.kaariboga.core.Kaariboga;
import org.kaariboga.core.KaaribogaEvent;


/**
 * The Executer is able to execute a program on a remote machine.
 * This one only works when the server is started with the
 * -soff option which disables the security manager:
 * java org.kaariboga.server.Boga -soff
 */
public class Executer extends Kaariboga
{
    /**
     * Runtime object of the current JVM.
     * Declared as an object varible to demonstrate the use of transient.
     */
    transient private Runtime rntime;

    /**
     * The command to be executed.
     */
    String command;

    /**
     * Creates a new executer and sets the command to execute.
     * For security reasons it is hard coded as the windows
     * paintbrush program.
     */
    public Executer (String name){
        super("Executer_" + name);
        this.command = "pbrush";
    }

    /**
     * Executes the given command if the base was started with the
     * security off option (-soff).
     */
    public void run(){
        System.out.println("Going to execute! " + command);
        rntime = Runtime.getRuntime();
        try{
            rntime.exec(command);
        }
        catch(IOException e){
            System.err.println("Error executing other program!");
        }
    }
}