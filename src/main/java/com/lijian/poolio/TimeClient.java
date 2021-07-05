package com.lijian.poolio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class TimeClient {

    public static void main(String[] args) throws InterruptedException {
        String host = "127.0.0.1";
        int port = 8083;
        timeClient(host, port);
    }

    public static void timeClient(String host, int port) {
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            // 发送查询当前时间指令
            out.println("QUERY CURRENT TIME ORDER");
            System.out.println("Client send order to server succeed.");
            // 返回应答
            String resp = in.readLine();
            System.out.println("Now is : " + resp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                out.close();
                out = null;
            }
            // 释放socket套接字句柄资源
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }

    }
}
