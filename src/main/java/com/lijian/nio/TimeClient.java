package com.lijian.nio;

public class TimeClient {

    public static void main(String[] args) {
        int port = 8084;
        new Thread(new TimeClientHandle("127.0.0.1", port), "NIO-TimeClient").start();
    }

}
