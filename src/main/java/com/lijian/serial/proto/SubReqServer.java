package com.lijian.serial.proto;

import com.lijian.protobuf.SubscribeReqProto;
import com.lijian.protobuf.SubscribeRespProto;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SubReqServer {

    public static void main(String[] args) throws Exception {
        int port = 8082;
        new SubReqServer().bind(port);
    }


    public void bind (int port) throws Exception {
        // NIO 线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // Java序列化编解码 ObjectDecoder ObjectEncoder
                        // ObjectDecoder对POJO对象解码，有多个构造函数，支持不同的ClassResolver，所以使用weakCachingConcurrentResolver
                        // 创建线程安全的WeakReferenceMap对类加载器进行缓存SubReqServer
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 半包处理 ProtobufVarint32FrameDecoder
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            // 添加ProtobufDecoder解码器，需要解码的目标类是SubscribeReq
                            socketChannel.pipeline().addLast(
                                    new ProtobufDecoder(SubscribeReqProto.SubscribeReq.getDefaultInstance()));
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            socketChannel.pipeline().addLast(new ProtobufEncoder());
                            socketChannel.pipeline().addLast(new SubReqServerHandler());
                        }
                    });
            // 绑定端口，同步等待成功
            ChannelFuture f = bootstrap.bind(port).sync();
            // 等待所有服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

        }

    }

}
