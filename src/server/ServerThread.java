package server;

import tools.ExceptionHandler;
import tools.IOHelper;
import tools.protocol.CommunicationException;

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
    private String serverBaseDir;
    private String myBaseDir;
    private final int TIMEOUT = 500;

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
    	int timeout = TIMEOUT;
        while (run){
            try {
                String s = recvMessage(in);
                if(s == null){
                	if(timeout-- < 0)
                		run = false;
                	Thread.sleep(10);
                }else{
                	timeout = TIMEOUT;
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
                }
            }catch (CommunicationException com){
                ExceptionHandler.handleException(com);
            }
            catch (Exception e)
            {
                if(e instanceof CommunicationException)
                    ExceptionHandler.handleException((CommunicationException)e);
                else
                    ExceptionHandler.handleException(e, ExceptionHandler.OccClass.SERVER_THREAD);
            }
        }
        ExceptionHandler.InfoMessage("ServerThread closed");
    }

    private void handshake()throws Exception{
        String hostname = recvMessage(in,"hostname");
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
    private void initMainSequence()throws Exception{
        ArrayList<tools.file.File> files = server.calcMetaData(hostname);
        sendMessage(out,"REPLY");
        if(files != null) {
            StringBuilder metadata = new StringBuilder();
            for (tools.file.File f : files)
                metadata.append(f.filename + " ");
            String msg = metadata.toString();
            if(msg == null || msg.equals("null") || msg.length() == 0)
            	sendMessage(out,"NULL");
            else
            	sendMessage(out,msg);
        }else{
        	sendMessage(out,"NULL");
        }
        if(!recvMessage(in,"ACK").equals("ACK"))
            throw new CommunicationException("Received wrong Message","ACK");
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
            if(!recvMessage(in,"NAME").equals("NAME"))
                throw new CommunicationException("Received wrong Message","NAME");
            String filename = recvMessage(in);
            if(!recvMessage(in,"SIZE").equals("SIZE"))
                throw new CommunicationException("Received wrong Message","SIZE");
            long fileSize = Long.valueOf(recvMessage(in));
            if(!recvMessage(in,"CONTENT").equals("CONTENT"))
                throw new CommunicationException("Received wrong Message","CONTENT");
            receiveContent(filename,fileSize);
            sendMessage(out,"ACK");
        }
        if(!recvMessage(in,"NULL").equals("NULL"))
            throw new CommunicationException("Received wrong Message","NULL");
        sendMessage(out,"ACK");
    }

    /**
     * Reads a File from the socket and saves it to the local filesystem
     * under the specified filename
     * @param filename
     * @return
     */
    private void receiveContent(String filename, long fileSize)throws Exception{
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
                throw new CommunicationException("FileSystemError"+filename,"ACK");
            }
            FileOutputStream out = new FileOutputStream(f);
            boolean b = IOHelper.writeToOutputStream(in,out,fileSize);
            if(b)
                out.close();
            else
                throw new CommunicationException("File to Disk Error");
        }catch (CommunicationException com) {
            ExceptionHandler.handleException(com);
        }catch (Exception e){
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.SERVER_THREAD);
        }
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
