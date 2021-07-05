package com.lijian.aio.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * 客户端连接异步处理器
 * completed()方法完成回调logic
 * failed()方法完成失败回调logic
 */
public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler> {

    /**
     * 调用该方法表示客户端已经介接入成功
     * 同时再accept接收新的客户端连接
     * @param result
     * @param attachment
     */
    @Override
    public void completed(AsynchronousSocketChannel result,
                          AsyncTimeServerHandler attachment) {
        // 此时还要继续调用accept方法是因为，completed方法表示上一个客户端连接完成，而下一个新的客户端需要连接
        // 如此形成新的循环：每接收一个客户端的成功连接之后，再异步接收新的客户端连接
        attachment.asynchronousServerSocketChannel.accept(attachment, this);
        // 预分配1M的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 调用read方法异步读，传入CompletionHandler类型参数异步回调读事件
        result.read(buffer, buffer, new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AsyncTimeServerHandler attachment) {
        exc.printStackTrace();
        attachment.latch.countDown();
    }
}
