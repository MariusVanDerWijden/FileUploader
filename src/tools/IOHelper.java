package tools;

import tools.protocol.CommunicationException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public abstract class IOHelper {


    private static final int BUFFER_SIZE = 8096; //write 8KB at once
    private static final boolean DEBUG = false;
    private static final int writePercentageFrom = 100*1024*1024; //display Percentage only if file is bigger than this

    /**
     * Sends a message over the socket
     * @param s
     */
    public static void sendMessage(OutputStream out, String s) throws Exception{
        if(DEBUG)System.out.println("SendMSG: "+s);
        for(int i = 0; i < s.length();i++){
            out.write(s.charAt(i));
        }
        out.write(0);
    }

    /**
     * Receives a message over the socket
     * @return
     */
    public static String recvMessage(InputStream in) throws Exception{
        int ch = in.read();
        StringBuilder sb = new StringBuilder();
        while (ch != -1 && ch != 0){
            sb.append((char)ch);
            ch = in.read();
        }
        String res = sb.length() == 0 ? null : sb.toString();
        if(DEBUG)System.out.println("RecvMsg: "+res);
        return res;
    }

    public static String recvMessage(InputStream in, String expectedValue)throws CommunicationException{
        try{
            return recvMessage(in);
        }catch (Exception e){
            CommunicationException com = new CommunicationException(e);
            com.occurrence = ExceptionHandler.OccClass.IO_HELPER;
            com.expectedValue = expectedValue;
            throw com;
        }
    }



    /**
     * Writes the content from in to out
     * @param in
     * @param out
     * @return
     */
    public static boolean writeToOutputStream(InputStream in, OutputStream out, long fileSize, boolean printStatus){
        try{
            byte[] buffer = new byte[BUFFER_SIZE];
            int i;
            int bytesRead = 0;
            int percentRead = 0;
            boolean b = (printStatus && fileSize > writePercentageFrom);
            while (bytesRead < fileSize && (i = in.read(buffer)) != -1) {
                if(b && bytesRead /(fileSize/100) > percentRead)
                    ExceptionHandler.InfoMessage("Written "+ ++percentRead +" percent");
                bytesRead += i;
                if(DEBUG)ExceptionHandler.InfoMessage("WriteToOutput: " + Arrays.toString(buffer));
                out.write(buffer, 0, i);
            }
            return true;
        }catch (Exception e){
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.IO_HELPER);
        }
        return false;
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
