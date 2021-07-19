package com.lijian.protocol.privateStack;

import com.lijian.protocol.privateStack.codec.PrivateProtocolMarshallingDecoder;
import com.lijian.protocol.privateStack.codec.PrivateProtocolMarshallingEncoder;
import com.lijian.protocol.privateStack.codec.ProtocolMessageDecoder;
import com.lijian.protocol.privateStack.codec.ProtocolMessageEncoder;
import com.lijian.protocol.privateStack.constant.NettyConstant;
import com.lijian.protocol.privateStack.handler.HeartBeatReqHandler;
import com.lijian.protocol.privateStack.handler.LoginAuthReqHandler;
import com.lijian.protocol.privateStack.message.PrivateProtocolMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * netty 私有协议栈客户端
 */
public class PrivateProtocolClient {

    public static final Logger log = LoggerFactory.getLogger(PrivateProtocolClient.class);

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);


    public static void main(String[] args) throws Exception {
        new PrivateProtocolClient().connect(NettyConstant.REMOTE_IP, NettyConstant.REMOTE_PORT);
    }

    public void connect(final String host, final int port) throws Exception {
        // NIO 线程组
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 解码器：防止消息过大导致内存溢出或者畸形码流引起内存分配失败，所以对单条消息最大长度进行限制
                            socketChannel.pipeline().addLast(new ProtocolMessageDecoder(1024 * 1024, 4, 4));
                            // 编码器
                            socketChannel.pipeline().addLast("messageEncoder", new ProtocolMessageEncoder());
                            // 添加超时处理handler，规定时间内没有收到消息则关闭链路，重新尝试连接
                            socketChannel.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(50));
                            // 登录认证请求处理器
                            socketChannel.pipeline().addLast("loginAuthHandler", new LoginAuthReqHandler());
                            // 心跳检查请求处理器
                            socketChannel.pipeline().addLast("heartbeatHandler", new HeartBeatReqHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(
                    new InetSocketAddress(host, port),
                    new InetSocketAddress(NettyConstant.LOCAL_IP, NettyConstant.LOCAL_PORT)).sync();
            future.channel().closeFuture().sync();
        } finally {
            // 释放完毕后，清空资源，再次发起重连操作
            executorService.execute(() -> {
                try {
                    log.info("The Client retry connecting...");
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    log.error("The Client retry connect failed: ", e);
                }
                try {
                    connect(NettyConstant.REMOTE_IP, NettyConstant.REMOTE_PORT);
                } catch (Exception e) {
                    log.error("The Client retry connect failed: ", e);
                }
            });
        }

    }

}
