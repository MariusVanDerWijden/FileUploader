package server;

import tools.ExceptionHandler;

public class Main {

    public static void main(String args[]){
        int port = Integer.valueOf(args[0]);
        String hostDir = args[1];
        ExceptionHandler.InfoMessage("Starting Server");
        new Server(port,hostDir);
    }
}