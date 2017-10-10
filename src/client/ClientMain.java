package client;

import tools.ExceptionHandler;

public class ClientMain {

    public static void main(String[] args){
        String ip = args[0];
        int port = Integer.valueOf(args[1]);
        String hostDir = args[2];
        ExceptionHandler.InfoMessage("Starting Client");
        new Client(ip,port,hostDir);
    }

}
