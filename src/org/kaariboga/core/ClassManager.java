package org.kaariboga.core;

import java.io.*;
import java.util.Hashtable;
import java.util.StringTokenizer;


/**
 * Stores the classes and the bytecodes of the loaded classes.
 * It is responsible to remove the classes if there is no longer
 * any kaariboga of that kind active.
 */
public class ClassManager
{
    /**
     * Helper class for the ClassManager to wrap class and bytecodes
     * of the loaded classes.
     */
    class ClassBox{

        /**
         * The loaded class.
         */
        public Class classCode;

        /**
         * The korresponding bytecode of a loaded class.
         */
        public byte[] byteCode;

        /**
         * How many agents of this kind are loaded.
         */
        public int count;

        /**
         * Creates a new ClassBox.
         * The count is set to 1.
         *
         * @param cl The class to be stored.
         * @param byteCode The byte code of the class.
         */
        public ClassBox (Class cl, byte[] byteCode){
            this.classCode = cl;
            this.byteCode = byteCode;
            count = 1;
        }
    }

    /**
     * Helper class for the ClassManager that removes a class
     * after a given delay time.
     */
    class Remover extends Thread{

        /**
         * Name of the class to be removed.
         */
        private String name;

        /**
         * Delay time in milliseconds after which the class is
         * removed if it's cout = 0;
         */
        private long delay;

        public Remover (String name, long delay){
            this.delay = delay;
            this.name = name;
        }

        /**
         * Waits for the specified delay time and then removes
         * the class from the cache if it's count = 0;
         */
        public void run(){
            try {
                Thread.sleep(delay);
                // oo better lock the whole cache during this operation
                if ( ((ClassBox)cache.get(name)).count == 0)
                    cache.remove(name);
            }
            catch (InterruptedException e){
                System.err.println ("Remover Thread was interrupted!");
            }
        }
    }

     /**
      * The path where the agents class files are.
      */
    public File[] agentsPath = null;

    /**
     * Stores class data in ClassBoxes.
     */
    Hashtable cache;

    /*
     * Time to wait in milliseconds before a class is removed from
     * the ClassManager after it's count = 0.
     */
    long delay;

    /**
     * Create a new ClassManager.
     *
     * @param delay Time in milliseconds the ClassManager waits before a
     * class is removed after it's count = 0.
     * @param path The classpath from which classes are sent to other
     * bases.
     */
    public ClassManager(long delay, String[] path){
        cache = new Hashtable();
        this.delay = delay;

        agentsPath = new File [ path.length ];
        for (int i = 0; i < path.length; i++) {
            agentsPath[i] = new File ( path[i] );
        }
    }

    /**
     * Adds a new class to the class manager.
     *
     * @param name Name of the class.
     * @param cl The class istself.
     * @param byteCode Byte code of the class.
     */
    public void addClass (String name, Class cl, byte[] byteCode){
        ClassBox cb = new ClassBox (cl, byteCode);
        cache.put (name, cb);
    }

    /**
     * Removes a class and it's bytecode from the class manager
     *
     * @param name Name of the class to be removed
     */
    public void removeClass (String name){
        cache.remove(name);
    }

    /**
     * Returns the class of a given name
     */
    public Class getClass (String name){
    	ClassBox box = (ClassBox) cache.get(name);
	    if (box != null){
	        return box.classCode;
	    }
	    else return null;
    }

    /**
     * Returns the byte code of a class
     */
    public byte[] getByteCode (String name){
        try {
    	
	    ClassBox box = (ClassBox) cache.get(name);
	    if (box != null){
	        return box.byteCode;
	    }
	    else { // try to load class from file
                
             // determine file name
                String fileName = name.replace( '.', File.separatorChar );
                fileName = fileName + ".class"; 

             // search directories for class file
	        File classfile = null;
		for( int i=0; i<agentsPath.length; ++i )
		{
			File tmpf = new File( agentsPath[i], fileName );
			if( tmpf.exists() )
			{
				classfile = tmpf;
				break;
			}
		}
		if( classfile == null ) return null; 	
		System.out.println("Filename: " + classfile.getCanonicalPath() );

            // load byte code from file
                FileInputStream fileStream = new FileInputStream(classfile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte [4096];
                int len;
                while (  ( len = fileStream.read(buffer) ) > 0  )
                {
                    bos.write (buffer, 0, len);
                }
                byte[] byteCode = bos.toByteArray();
                bos.close();
                fileStream.close();
                return byteCode;
            }
	}
        catch (Exception e){
                System.err.println ("! ClassManager.getByteCode: " + e );
            	return null;
	}
    }

    /**
     * Increases the count of class name
     */
    public void inc(String name){
        ClassBox box = (ClassBox) cache.get(name);
        if (box != null){
            box.count++;
        }
    };

    /**
     * Decreases the count of class name
     */
    public void dec(String name){
        ClassBox box = (ClassBox) cache.get(name);
        if (box != null){
            box.count--;
            if (box.count == 0){
                // remove class after a certain time
                (new Remover(name, delay)).start();
            }
        }
    }

}
