package client;

import tools.ExceptionHandler;
import tools.file.FileFinder;
import tools.protocol.CommunicationException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.ArrayList;

import static tools.IOHelper.recvMessage;
import static tools.IOHelper.sendMessage;

public class Client {

    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private String hostname;
    private FileFinder fileFinder;
    private ArrayList<String> filesAlreadySend;

    public Client(String server, int port, String hostDirectory){
        try {
            fileFinder = new FileFinder(hostDirectory);
            socket = new Socket(server,port);
            out = socket.getOutputStream();
            in = socket.getInputStream();
            hostname = Inet4Address.getLocalHost().getHostAddress();
            if(hostname == null)
                hostname = "host1"; //TODO change this!
            startCommunication();
        }catch (Exception e) {
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.CLIENT);
        }
    }



    /**
     * Initializes the communication
     */
    private void startCommunication(){
        try {
        if(!handshake())return;
        String metadata = initMainSequence();
        if(metadata == null) return;
        parseMetaData(metadata);
        ArrayList<tools.file.File> filesToSend = fileFinder.compileListOfFiles(filesAlreadySend);
        if(filesToSend.size()==0){
            endCommunication();
            return;
        }
        sendData(filesToSend);
        endCommunication();

        }catch (CommunicationException com){
            ExceptionHandler.handleException(com);
        }catch (Exception e){
            if(e instanceof  CommunicationException)
                ExceptionHandler.handleException((CommunicationException) e);
            else
                ExceptionHandler.handleException(e, ExceptionHandler.OccClass.CLIENT);
        }
    }

    /**
     * Performs the handshake with the server connected on socket
     * may change the hostname, if such a hostname already exists on the server
     */
    private boolean handshake() throws Exception{
        sendMessage(out,"SYN");
        sendMessage(out,hostname);
        String s = recvMessage(in,"ACK|NEW");
        if(s.equals("ACK"))
            return true;
        if(s.equals("NEW")){ //Sets a new Hostname for further communication
            hostname = recvMessage(in,"hostname");
            return true;
        }
        throw new CommunicationException("Received wrong Message","ACK|NEW");
    }

    /**
     * Client requests Metadata from the server and calculates which files to upload
     * returns true if the server has send correct data
     */
    private String initMainSequence()throws Exception{
        sendMessage(out,"REQ");
        String reply = recvMessage(in,"REPLY");
        if(reply.equals("REPLY")){
            String metadata = recvMessage(in,"metadata");
            sendMessage(out,"ACK");
            return metadata;
        }
        throw new CommunicationException("Received wrong Message","REPLY");
    }

    /**
     * Parses the received metadata (sends all if metadata equals NULL)
     * returns a list of files that have to be send
     * @param metadata
     */
    private void parseMetaData(String metadata){
        if(metadata.equals("NULL")){
            filesAlreadySend = null;
        }else{
            filesAlreadySend = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < metadata.length(); i++){
                char ch = metadata.charAt(i);
                if(ch==' ') {
                    filesAlreadySend.add(sb.toString().replaceAll("/"+hostname,""));
                    sb.setLength(0);
                }else
                    sb.append(ch);
            }
        }
    }

    /**
     * Sends the files that shall be updated one by one
     * @param listOfFiles
     * @return
     */
    private boolean sendData(ArrayList<tools.file.File> listOfFiles)throws Exception{
        sendMessage(out,"FILES");
        sendMessage(out,""+listOfFiles.size());
        if(!recvMessage(in).equals("ACK")) return false;
        for(tools.file.File f : listOfFiles){
            sendMessage(out,"NAME");
            sendMessage(out,f.filename);
            sendMessage(out,"SIZE");
            sendMessage(out,Long.toString(f.content.length()));
            sendMessage(out,"CONTENT");
            if(!f.writeToOutputStream(out))
                throw new CommunicationException("Couldn't write to OutputStream");
            if(!recvMessage(in,"ACK").equals("ACK"))
                throw new CommunicationException("Received wrong Message","ACK");
        }
        sendMessage(out,"NULL");
        return recvMessage(in,"ACK").equals("ACK");
    }

    private boolean endCommunication()throws Exception{
        sendMessage(out,"CLOSE");
        String s = recvMessage(in,"CLOSE_ACK");
        if(!s.equals("CLOSE_ACK")){
            throw new CommunicationException("Received wrong Message","CLOSE_ACK");
        }
        try {
            in.close();
            out.close();
            socket.close();
        }catch (Exception e){
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.CLIENT);
            return false;
        }
        return true;
    }

}