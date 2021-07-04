package com.lijian.protocol.privateStack;

import com.lijian.protocol.privateStack.codec.ProtocolMessageDecoder;
import com.lijian.protocol.privateStack.codec.ProtocolMessageEncoder;
import com.lijian.protocol.privateStack.constant.NettyConstant;
import com.lijian.protocol.privateStack.handler.HeartBeatRespHandler;
import com.lijian.protocol.privateStack.handler.LoginAuthRespHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateProtocolServer {

    public static final Logger log = LoggerFactory.getLogger(PrivateProtocolServer.class);

    public static void main(String[] args) throws Exception {
        new PrivateProtocolServer().bind();
    }


    public void bind() throws Exception {
        // NIO 线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            // 解码器
                            socketChannel.pipeline().addLast(new ProtocolMessageDecoder(1024 * 1024, 4, 4));
                            // 编码器
                            socketChannel.pipeline().addLast(new ProtocolMessageEncoder());
                            // 超时处理器，默认50s超时时间，如果是客户端则需要重新发起连接，如果是服务端则需要释放资源，清空客户端缓存
                            socketChannel.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(50));
                            // 登录认证（握手）应答处理器
                            socketChannel.pipeline().addLast("loginAuthRespHandler", new LoginAuthRespHandler());
                            // 心跳检测应答处理器
                            socketChannel.pipeline().addLast("heartBeatHandler", new HeartBeatRespHandler());
                        }
                    });
            // 绑定端口，同步等待成功
            ChannelFuture f = bootstrap.bind(NettyConstant.REMOTE_IP, NettyConstant.REMOTE_PORT).sync();
            log.info("Server[{}] start success", NettyConstant.REMOTE_IP + ": " + NettyConstant.REMOTE_PORT);
            // 等待所有服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

}
