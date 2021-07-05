package com.lijian.aio.server;

import java.io.IOException;


/**
 * AIO 异步非阻塞服务端
 * 不需要单独开线程去处理read、write等事件
 * 只需要关注complete-handlers中的回调completed方法
 */
public class TimeServer {

    public static void main(String[] args) throws IOException {
        int port = 8086;
        AsyncTimeServerHandler timeServer = new AsyncTimeServerHandler(port);
        new Thread(timeServer, "AIO-AsyncTimeServerHandler").start();
    }
}
