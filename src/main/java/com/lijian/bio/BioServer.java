package com.lijian.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO通信服务端:
 * 由一个独立的Acceptor线程负责监听客户客户端的连接，
 * 接收到客户端连接请求之后为每个客户端创建一个新的线程进行链路处理，
 * 处理完之后通过输出流返回应答给客户端，最后线程销毁，这是典型的一请求一应答通信模型
 */
public class BioServer {

    public static void main(String[] args) {
        bioServer(8082);
    }

    /**
     * @param port
     */
    public static void bioServer(int port) {
        ServerSocket server = null;
        try {
            // ServerSocket负责绑定IP地址，启动监听端口
            server = new ServerSocket(port);
            System.out.println("The bio server is start in port : " + port);
            // Socket负责发起连接操作
            Socket socket = null;
            // 无限循环监听客户端的连接，若没有则主线程阻塞在ServerSocket的accept操作上
            while (true) {
                socket = server.accept();
                new Thread(new BioServerHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                System.out.println("The bio server close");
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            server = null;
        }
    }
}
