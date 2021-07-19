package com.lijian.aio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * 服务端AIO异步处理任务
 * -创建异步服务通道监听端口
 * -监听客户端连接
 */
public class AsyncTimeServerHandler implements Runnable{

    private int port;

    CountDownLatch latch;
    AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public AsyncTimeServerHandler(int port) {
        this.port = port;
        try {
            // 创建异步的服务通道asynchronousServerSocketChannel, 并bind监听端口
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
            System.out.println("The time server is start in port : " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // countDownLatch没有count减一，所以导致一直阻塞
        latch = new CountDownLatch(1);
        doAccept();
        try {
            // 防止执行操作线程还未结束，服务端线程就退出，程序不退出的前提下，才能够让accept继续可以回调接受来自客户端的连接
            // 实际开发过程中不需要单独开启线程去处理AsynchronousServerSocketChannel
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收客户端的连接
     * 参数CompletionHandler类型的handler实例来接收accept操作成功的通知消息
     */
    public void doAccept() {
        asynchronousServerSocketChannel.accept(this, new AcceptCompletionHandler());
    }
}
