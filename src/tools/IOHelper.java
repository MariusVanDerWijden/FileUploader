package tools;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public abstract class IOHelper {


    private static final int BUFFER_SIZE = 8096; //write 8KB at once
    private static final int TIMEOUT = 1000000; //prevents us from infinitely waiting
    private static final boolean DEBUG = true;

    /**
     * Sends a message over the socket
     * @param s
     */
    public static void sendMessage(OutputStream out, String s){
        if(DEBUG)System.out.println("SendMSG: "+s);
        try{
            for(int i = 0; i < s.length();i++){
                out.write(s.charAt(i));
            }
            out.write(0);
        }catch (Exception e) {
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.IO_HELPER);
        }
    }

    /**
     * Receives a message over the socket
     * @return
     */
    public static String recvMessage(InputStream in){
        try{
            int ch = in.read();
            StringBuilder sb = new StringBuilder();
            while (ch != -1 && ch != 0){
                sb.append((char)ch);
                ch = in.read();
            }
            String res = sb.length() == 0 ? null : sb.toString();
            if(DEBUG)System.out.println("RecvMsg: "+res);
            return res;
        }catch (Exception e) {
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.IO_HELPER);
        }
        return null;
    }

    /**
     * Writes the content from in to out
     * @param in
     * @param out
     * @return
     */
    public static boolean writeToOutputStream(InputStream in, OutputStream out, long fileSize){
        try{
            byte[] buffer = new byte[BUFFER_SIZE];
            int i;
            int bytesRead = 0;
            while (bytesRead < fileSize && (i = in.read(buffer)) != -1) {
                bytesRead += i;
                if(DEBUG)System.out.println("WriteToOutput: " + Arrays.toString(buffer));
                out.write(buffer, 0, i);
            }
            return true;
        }catch (Exception e){
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.IO_HELPER);
        }
        return false;
    }

    public static boolean writeToOutputStream(InputStream in, OutputStream out){
        return writeToOutputStream(in,out,Long.MAX_VALUE);
    }

    /**
     * Tries to make a directory with the given name
     * @param dirName
     */
    public static void makeDirectory(String dirName){
        try {
            File dir = new File(dirName);
            if(!dir.exists())
                dir.mkdir();
        }catch (Exception e){
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.IO_HELPER);
        }
    }


}
