package com.lijian.pack.delimitter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Logger;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = Logger.getLogger(TimeClientHandler.class.getName());

    private int counter;
    private byte[] req;

    public TimeClientHandler() {
        // 以$_为分隔符，发送命令
        req = ("QUERY TIME ORDER$_").getBytes();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf message = null;
        // 循环发送100条消息，每发送一条刷新一次，服务端理论上接收到100条查询时间指令的请求
        for (int i = 0; i < 100; i++) {
            message = Unpooled.buffer(req.length);
            message.writeBytes(req);
            ctx.writeAndFlush(message);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 不需要编解码了，直接返回了字符串的应答消息
        String body = (String) msg;
        // 客户端每接收到服务端一条应答消息之后，计数器就加1，理论上应该有100条服务端日志
        System.out.println("Now is: " + body + "; the current is "+ (++counter));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warning("Unexpected exception from downstream: " + cause.getMessage());
        ctx.close();
    }
}
