package tools;

import java.net.SocketException;

public class ExceptionHandler {

    /**
     * Specifies in which class the error occurred
     */
    public enum OccClass {CLIENT, CLIENT_MAIN, SERVER, SERVER_MAIN, FILE_FINDER, FILE, SERVER_THREAD,IO_HELPER }

    /**
     * Handles Exceptions, maybe rewrite to use message boxes
     * @param e
     */
    public static void handleException(Exception e,OccClass cla){
        if(e instanceof SocketException) System.exit(1);
        System.out.println(cla.toString());
        e.printStackTrace();

    }

    public static void InfoMessage(String msg){
        System.out.println(msg);
    }
}
