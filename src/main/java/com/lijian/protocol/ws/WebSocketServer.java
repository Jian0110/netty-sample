package com.lijian.protocol.ws;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketServer {


    public static void main(String[] args) throws Exception {
        int port = 8888;
        new WebSocketServer().run(port);
    }


    public void run(int port) throws Exception {
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
                            socketChannel.pipeline().addLast("http-codec", new HttpServerCodec());
                            // HttpObjectAggregator将多个消息转换为单一的FullHttpRequest
                            socketChannel.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65563));
                            socketChannel.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                            // Chunked handler支持异步发送大的码流（大文件传输）,来向客户端发送HTML5文件
                            socketChannel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                            socketChannel.pipeline().addLast("handler", new WebSocketServerHandler());
                        }
                    });
            // 绑定端口，同步等待成功
            ChannelFuture f = bootstrap.bind(port).sync();
            System.out.println("Web socket server is started at port: "+port+".");
            System.out.println("Open your browser and navigate to http://localhost:"+port+"/");
            // 等待所有服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();

        }

    }

}
