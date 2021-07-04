package com.lijian.http;

import com.lijian.protobuf.SubscribeReqProto;
import com.lijian.serial.proto.SubReqServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServer {

    public static final String DEFAULT_URL="";

    public static void main(String[] args) throws Exception {
        int port = 8082;
        String url = DEFAULT_URL;
        new HttpFileServer().run(port, url);
    }


    public void run(int port, String url) throws Exception {
        // NIO 线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // Java序列化编解码 ObjectDecoder ObjectEncoder
                        // ObjectDecoder对POJO对象解码，有多个构造函数，支持不同的ClassResolver，所以使用weakCachingConcurrentResolver
                        // 创建线程安全的WeakReferenceMap对类加载器进行缓存SubReqServer
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            // 添加ProtobufDecoder解码器，需要解码的目标类是SubscribeReq
                            socketChannel.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                            // HttpObjectAggregator将多个消息转换为单一的FullHttpRequest
                            socketChannel.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65563));
                            socketChannel.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                            // Chunked handler支持异步发送大的码流（大文件传输）
                            socketChannel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                            socketChannel.pipeline().addLast("fileServerHandler", new SubReqServerHandler());
                        }
                    });
            // 绑定端口，同步等待成功
            ChannelFuture f = bootstrap.bind("192.168.1.102", port).sync();
            System.out.println("Http file server is running, website is: http://192.168.1.102:"+port+url);
            // 等待所有服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

        }

    }

}
