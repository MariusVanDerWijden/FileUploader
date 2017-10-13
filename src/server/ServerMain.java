package server;

import tools.ExceptionHandler;

public class ServerMain {

    public static void main(String args[]){
        if(args.length != 2) {
            printHelp();
            System.exit(1);
        }
        try {
            int port = Integer.valueOf(args[0]);
            String hostDir = args[1];
            ExceptionHandler.InfoMessage("Starting Server");
            new Server(port,hostDir);
        }catch (Exception e){
            printHelp();
        }
    }


    private static void printHelp(){
        ExceptionHandler.InfoMessage("Usage:");
        ExceptionHandler.InfoMessage("first argument: port");
        ExceptionHandler.InfoMessage("second argument: directory to save files");
    }
}