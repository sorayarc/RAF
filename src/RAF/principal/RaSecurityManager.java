package raf.principal;

import java.io.FileDescriptor;


public final class RaSecurityManager extends SecurityManager{

    private boolean secure;

    public RaSecurityManager(boolean secure){
        super();
        this.secure = secure;
    }
    
   
    public void checkAccept(String host, int port){
    
    }

    
    public void checkConnect(String host, int port){
    }

   
    public void checkListen(int port){
    }
    
    // Threads
    
    
    public void checkAccess(Thread thread){
    }

    /**
     * At the moment this is always allowed!
     */
    public void checkAccess(ThreadGroup threadGroup){
    }
    
    /**
     * Checks if a class may create it's own class loader.
     * At the moment this is always allowed!
     */
    public void checkCreateClassLoader(){
    }
    
    /**
     * Checks if a class may delete files.
     * Only allowed if security = false !
     */
    public void checkDelete(String filename){
        if (secure) throw new SecurityException("No file access!");
    }
    
    /**
     * Checks if a class may read files.
     * Always allowed!
     */
    public void checkRead(String filename){
    }
    
    /**
     * Checks if writing files is allowed.
     * Only allowed if security = false !
     */
    public void checkWrite(String filename){
        if (secure) throw new SecurityException("No file write access!");
    }

    /**
     * Checks if a class may write files with a give file descriptor.
     * Always allowed, needed for writing to streams.
     */
    public void checkWrite(FileDescriptor fd){
    }

    /**
     * Checks if a class may read files through a file descriptor.
     * Always allowed.
     */
    public void checkRead(FileDescriptor fd){}

    // system   

    /**
     * Checks if a class may execute a system command.
     * Only allowed if security = false !
     */
    public void checkExec(String command){
        if (secure) throw new SecurityException("No program execution allowed!");
    }

    /**
     * Checks if a class is allowed to end the virtual machine.
     * At the moment always allowed, because it is better for 
     * test purposes.
     */
    public void checkExit(int status){
    }
    
    // properties

    /**
     * Checks if access to system properties is allowed.
     * Alway allowed !
     */
    public void checkPropertiesAccess(){
    }

    /**
     * Checks if access to system properties is allowed.
     * Alway allowed !
     */
    public void checkPropertyAccess(String key){
    }

    /**
     * Checks if access to system properties is allowed.
     * Alway allowed !
     */
    public void checkPropertyAccess(String key, String def){
    }

    /**
     * Alway returns true.
     */
    public boolean checkTopLevelWindow(Object window){
        return true;
    }
    
    /**
     * Always allowed!
     */
    public void checkAwtEventQueueAccess(){}

    /**
     * Always allowed!
     */
    public void checkLink(String lib){}

    /**
     * Always allowed!
     */
    public void checkPackageAccess(String pkg){}

    /**
     * Always allowed!
     */
    public void checkPackageDefinition(String pkg){}

    /**
     * Always allowed!
     */
    public void checkSecurityAccess(String action){}

    /**
     * Always allowed!
     */
    public void checkSetFactory(){}
}