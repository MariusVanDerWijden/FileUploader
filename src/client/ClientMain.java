package client;

import tools.ExceptionHandler;

public class ClientMain {

    public static void main(String[] args){
        if(args.length != 3) {
            printHelp();
            System.exit(1);
        }
        try {
            String ip = args[0];
            int port = Integer.valueOf(args[1]);
            String hostDir = args[2];
            ExceptionHandler.InfoMessage("Starting Client");
            new Client(ip, port, hostDir);
        }catch (Exception e){
            printHelp();
        }
    }

    private static void printHelp(){
        ExceptionHandler.InfoMessage("Usage:");
        ExceptionHandler.InfoMessage("first argument: ip of server");
        ExceptionHandler.InfoMessage("second argument: port");
        ExceptionHandler.InfoMessage("second argument: directory to be synced");
    }

}
