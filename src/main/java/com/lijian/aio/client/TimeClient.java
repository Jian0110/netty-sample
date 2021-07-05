package com.lijian.aio.client;

/**
 * AIO 异步非阻塞 客户端
 * 不需要单独开线程去处理read、write等事件
 * 只需要关注complete-handlers中的回调completed方法
 */
public class TimeClient {
    public static void main(String[] args) {
        int port = 8086;
        new Thread(new AsyncTimeClientHandler("127.0.0.1", port), "AIO-AsyncTimeClientHandler").start();

    }
}