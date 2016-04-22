package raf.principal;

import java.io.*;
import java.lang.String;
import java.lang.StringIndexOutOfBoundsException;
import java.lang.Thread;
import java.util.Hashtable;


/**
 * Almacena las clases y los bytecodes de las clases cargadas.
 * Es responsable de borrar las clases si no hay mas agentes
 * activos de esa clase en la agencia.
 */
public class ClassManager
{
    /**
     * Clase auxiliar del ClassManager para encapsular las clases y los bytecodes
     * de las clases cargadas.
     */
    class ClassBox{

        /**
         * La clase cargada.
         */
        public Class classCode;

        /**
         * Los correspondientes bytecodes de la clase cargada.
         */
        public byte[] byteCode;

        /**
         * Cuantos agentes de esta clase estan cargados.
         */
        public int count;

        /**
         * Crea un nuevo ClassBox.
         * El contador es puesto a 1.
         *
         * @param cl La clase que va a ser almacenada.
         * @param byteCode El byte code de la clase.
         */
        public ClassBox (Class cl, byte[] byteCode){
            this.classCode = cl;
            this.byteCode = byteCode;
            count = 1;
        }
    }

    /**
     * Clase auxiliar del ClassManager que borra una clase despues del tiempo de retardo.
     */
    class Remover extends Thread{

        /**
         * Nombre de la clase que va a ser borrada.
         */
        private String name;

        /**
         * Tiempo de retardo en milisegundos despues del cual la clase es borrada si su contador llega a cero.
         */
        private long delay;

        public Remover (String name, long delay){
            this.delay = delay;
            this.name = name;
        }

        /**
         * Espera el tiempo de retardo especificado y entonces borra la clase de la cache si el contador llega cero;
         */
        public void run(){
            try {
                Thread.sleep(delay);
                // mejor bloquear la cache durante esta operacion
                if ( ((ClassBox)cache.get(name)).count == 0)
                    cache.remove(name);
            }
            catch (InterruptedException e){
                System.err.println ("El thread Remover ha sido interrumpido!");
            }
        }
    }

     /**
      * El camino donde estan los ficheros de los agentes.
      */
     public String agentsPath = null;

    /**
     * Stores class data in ClassBoxes.
     */
    Hashtable cache;

    /*
     * Tiempo de espera en milisegundos antes de que se borre una clase del
     * ClassManager despues de que su contador llegue a cero.
     */
    long delay;

    /**
     * Crea un nuevo ClassManager.
     *
     * @param delay Tiempo en milisegundos que el ClassManager espera antes de que una
     * clase sea borada despues de que su contador haya llegado a cero.
     */
    public ClassManager(long delay, String agentsPath){
        cache = new Hashtable();
        this.delay = delay;
        this.agentsPath = agentsPath;
    }

    /**
     * Añade una nueva clase al class manager.
     *
     * @param name Nombre de la clase.
     * @param cl La clase en si misma.
     * @param byteCode Byte code de la clase.
     */
    public void addClass (String name, Class cl, byte[] byteCode){
        ClassBox cb = new ClassBox (cl, byteCode);
        cache.put (name, cb);
    }

    /**
     * Borra una clase y su bytecode desde el class manager
     *
     * @param name Nombre de la clase que va a ser borrada
     */
    public void removeClass (String name){
        cache.remove(name);
    }

    /**
     * Borra la clase indicada
     */
    public Class getClass (String name){
    	ClassBox box = (ClassBox) cache.get(name);
	    if (box != null){
	        return box.classCode;
	    }
	    else return null;
    }

    /**
     * DEvuelve el byte code de una clase
     */
    public byte[] getByteCode (String name){
    	ClassBox box = (ClassBox) cache.get(name);
	    if (box != null){
	        return box.byteCode;
	    }
	    else { // intenta cargar desde un fichero el nombre del fichero indicado
            int begin = 12; // longitud del raf.agentes.;
            String fileName;
            try {
                fileName = agentsPath + name.substring(begin) + ".class";
                System.out.println("Fichero: " + fileName);
            }
            catch (StringIndexOutOfBoundsException e){
                System.err.println ("ClassManager: Nombre de fichero inválido!");
                fileName = name;
            }

            // carga el byte code desde un fichero
            try {
                FileInputStream fileStream = new FileInputStream(fileName);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int data;
                while ( (data = fileStream.read()) != -1 ){
                    bos.write(data);
                }
                byte[] byteCode = bos.toByteArray();
                bos.close();
                fileStream.close();
                return byteCode;
            }
            catch (IOException e){
                return null;
            }
	    }
    }

    /**
     * Incrementa el contador de la clase
     */
    public void inc(String name){
        ClassBox box = (ClassBox) cache.get(name);
        if (box != null){
            box.count++;
        }
    };

    /**
     * Decrementa el contador de la  clase
     */
    public void dec(String name){
        ClassBox box = (ClassBox) cache.get(name);
        if (box != null){
            box.count--;
            if (box.count == 0){
                // borra la clase despues de cierto tiempo
                (new Remover(name, delay)).start();
            }
        }
    }

}
