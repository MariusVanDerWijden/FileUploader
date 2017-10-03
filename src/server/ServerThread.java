package server;

import tools.ExceptionHandler;
import tools.IOHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import static tools.IOHelper.recvMessage;
import static tools.IOHelper.sendMessage;


public class ServerThread extends Thread{

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private boolean run = true;
    private Server server;
    private String hostname;
    private final int BUFFER_SIZE = 8096;
    private String serverBaseDir;
    private String myBaseDir;


    public ServerThread(Server server, Socket socket, String serverBaseDir){
        super();
        ExceptionHandler.InfoMessage("ServerThread opened");
        try {
            this.server = server;
            this.socket = socket;
            in = socket.getInputStream();
            out = socket.getOutputStream();
            this.serverBaseDir = serverBaseDir;
        }catch (Exception e){
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.SERVER_THREAD);
            stopThis();
        }
    }

    public void run(){
        while (run){
            try {
                String s = recvMessage(in);
                switch (s) {
                    case "SYN":
                        handshake();
                        break;
                    case "REQ":
                        initMainSequence();
                        break;
                    case "FILES":
                        sendData();
                        break;
                    case "CLOSE":
                        sendMessage(out, "CLOSE_ACK");
                        stopThis();
                }
            }catch (Exception e)
            {
                ExceptionHandler.handleException(e, ExceptionHandler.OccClass.SERVER_THREAD);
            }
        }
        ExceptionHandler.InfoMessage("ServerThread closed");
    }

    private void handshake(){
        String hostname = recvMessage(in);
        if(server.isHostNameUnique(hostname)) {
            sendMessage(out, "ACK");
        }else {
            sendMessage(out, "NEW");
            hostname = server.genNewHostname();
            sendMessage(out,hostname);
        }
        this.hostname = hostname;
        generateDirectory();
    }

    /**
     * Calculates which files are already on the server
     * and sends a list of those to the client
     */
    private void initMainSequence(){
        ArrayList<tools.file.File> files = server.calcMetaData(hostname);
        sendMessage(out,"REPLY");
        if(files != null) {
            StringBuilder metadata = new StringBuilder();
            for (tools.file.File f : files)
                metadata.append(f.filename + " ");
            sendMessage(out,metadata.toString());
        }else{
            sendMessage(out,"NULL");
        }
        if(!recvMessage(in).equals("ACK"))
            return; //TODO what to do with errors?
    }

    /**
     * Contrary to the name:
     * this method receives Data send from the client
     * and saves it to the appropriate directory
     */
    private void sendData() throws Exception{
        int fileCount = Integer.valueOf(recvMessage(in));
        sendMessage(out,"ACK");
        for(int i = 0; i < fileCount; i++){
            if(!recvMessage(in).equals("NAME"))
                return; //TODO ERROR
            String filename = recvMessage(in);
            if(!recvMessage(in).equals("SIZE"))
                return; //TODO ERROR
            long fileSize = Long.valueOf(recvMessage(in));
            if(!recvMessage(in).equals("CONTENT"))
                return; //TODO ERROR
            boolean b = receiveContent(filename,fileSize);
            if(!b) throw new Exception("receiveContent returned false");
            sendMessage(out,"ACK");
        }
        if(!recvMessage(in).equals("NULL"))
            return;//TODO ERROR
        sendMessage(out,"ACK");
    }

    /**
     * Reads a File from the socket and saves it to the local filesystem
     * under the specified filename
     * @param filename
     * @return
     */
    private boolean receiveContent(String filename, long fileSize){
        byte[] buffer = new byte[BUFFER_SIZE];
        File f = new File(myBaseDir+filename);
        try{
            try {
                String s = myBaseDir+filename.substring(0, filename.lastIndexOf('/'));
                File dirs = new File(s);
                dirs.mkdirs();
            }catch (IndexOutOfBoundsException e){
                //ignore
            }
            if(!f.createNewFile()) {
                System.out.println("#FileSystemError: "+f.getName());
                return false; //TODO file system error
            }
            FileOutputStream out = new FileOutputStream(f);
            boolean b = IOHelper.writeToOutputStream(in,out,fileSize);
            if(b)
                out.close();
            else throw new Exception("File to Disk Error");
            return b;
        }catch (Exception e){
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.SERVER_THREAD);
        }
        return false;
    }

    /**
     * Generates a new Directory for the files of the connected Client
     */
    private void generateDirectory(){
        myBaseDir = serverBaseDir+"/"+hostname+"/";
        IOHelper.makeDirectory(myBaseDir);
    }

    private void stopThis(){
        run = false;
        try {
            in.close();
            out.close();
            socket.close();
        }catch (Exception e){
            //ignore
        }
        server.unbind(this);
    }

}
