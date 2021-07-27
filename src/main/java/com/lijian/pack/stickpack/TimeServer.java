package com.lijian.pack.stickpack;

import com.lijian.protocol.privateStack.constant.NettyConstant;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeServer {

    public static final Logger log = LoggerFactory.getLogger(TimeServer.class);

    public static void main(String[] args) throws Exception {
        new TimeServer().bind();
    }


    public void bind() throws Exception {
        // NIO 线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new TimeServerHandler());
                        }
                    });
            // 绑定端口，同步等待成功
            ChannelFuture f = bootstrap.bind(NettyConstant.REMOTE_IP, NettyConstant.REMOTE_PORT).sync();
            log.info("Time server[{}] start success", NettyConstant.REMOTE_IP + ": " + NettyConstant.REMOTE_PORT);
            // 等待所有服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

}
