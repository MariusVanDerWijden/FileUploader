package server;

import tools.ExceptionHandler;
import tools.file.FileFinder;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    private final int MAX_THREADS = 3;

    private ServerSocket socket;
    private ArrayList<ServerThread> threads;
    private String baseDirectory;

    public Server(int port, String baseDirectory){
        try{
            this.baseDirectory = baseDirectory;
            socket = new ServerSocket(port);
            threads = new ArrayList<>();
            run();
        }catch (Exception e){
            ExceptionHandler.handleException(e, ExceptionHandler.OccClass.SERVER);
        }
    }

    private void run(){
        while (true) {
            try {
                Socket s = socket.accept();
                if(threads.size()>MAX_THREADS){
                    s.close();
                    continue;
                }
                ServerThread st = new ServerThread(this, s,baseDirectory);
                st.start();
                threads.add(st);
            } catch (Exception e) {
                ExceptionHandler.handleException(e, ExceptionHandler.OccClass.SERVER);
            }
        }
    }

    /**
     * C
     * @param st
     */
    public synchronized void unbind(ServerThread st){
        threads.remove(st);
        st.interrupt();
    }

    /**
     * Looks up whether a Host has already connected under this name
     * Is called from ServerThreads
     * @param hostname
     * @return
     */
    public synchronized boolean isHostNameUnique(String hostname){
        return true; //TODO implement me
    }

    /**
     * Generates a new host name for the client
     * @return
     */
    public synchronized String genNewHostname(){
        return null; //TODO implement me
    }

    /**
     * Searches for a directory with the specified hostname, returns all files in this directory
     * @param hostname
     * @return
     */
    public synchronized ArrayList<tools.file.File> calcMetaData(String hostname){
        FileFinder fileFinder = new FileFinder(baseDirectory+"/"+hostname+"/");
        return fileFinder.files;
    }
}
