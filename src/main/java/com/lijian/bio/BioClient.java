package com.lijian.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * BIO通信客户端:
 * 由一个独立的Acceptor线程负责监听客户客户端的连接，
 * 接收到客户端连接请求之后为每个客户端创建一个新的线程进行链路处理，
 * 处理完之后通过输出流返回应答给客户端，最后线程销毁，这是典型的一请求一应答通信模型
 */
public class BioClient {

    public static void main(String[] args) throws InterruptedException {
        String host = "127.0.0.1";
        int port = 8082;
//        while (true) {
//            Thread.sleep(5000);
            bioClient(host, port);
//        }
    }

    public static void bioClient(String host, int port) {
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
