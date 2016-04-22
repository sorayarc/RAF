package org.kaariboga.agents;

import java.lang.InterruptedException;
import java.lang.Thread;

import org.kaariboga.core.Kaariboga;


/**
 * The Reproducer agent is able to load other agents.
 */
public class Reproducer extends Kaariboga
{
    /**
     * Number of childs the agent will create.
     */
    public int nChilds = 3;

    /**
     * Just initialize the super class.
     *
     * @param name The name of the agent. This name has to be
     * unique. Normally the KaaribogaBase class provides some
     * method to generate a unique name.
     */
    public Reproducer(String name){
        super("Reproducer_" + name);
    }

    /**
     * After an initial sleep period (grow up) the agent gives birth to new childs.
     */
    public void run(){
        try{
            System.out.println ("Hurray, I am born.");
            Thread.currentThread().sleep(500);
            for (int i = 0; i < nChilds; i++){       
  	        Reproducer agent = new Reproducer(base.generateName());
  	        agent.nChilds = nChilds - 1;
                base.addKaaribogaOnCreation (agent, null);
            }
        }
        catch(java.lang.InterruptedException e){}

        System.out.println ("Agent is dies after " + nChilds + " childs.");
        fireDestroyRequest();
    }
}