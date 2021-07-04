package com.lijian.aio;

import com.lijian.bio.BioServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TimeServer {

    public static void main(String[] args) {
        int port = 8083;
        timeServer(port);
    }

    public static void timeServer(int port){
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            Socket socket = null;
            System.out.println("The time server is start in port : "+port);
            TimeServerHandlerExecutePool executor = new TimeServerHandlerExecutePool(50, 10000);
            while (true) {
                socket = server.accept();
                executor.execute(new BioServerHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                System.out.println("The time server close");
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                server = null;
            }
        }
    }

}
