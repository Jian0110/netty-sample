package com.lijian.pack.delimitter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;
import java.util.logging.Logger;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(TimeClientHandler.class.getName());


    private int counter;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 不需要对消息进行编解码，直接String读取
        String body = (String) msg;
        // 每收到一条消息计数器就加1, 理论上应该接收到100条
        System.out.println("The time server receive order: " + body + "; the counter is : "+ (++counter));
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ?
                new Date(System.currentTimeMillis()).toString():"BAD ORDER";
        // 返回客户端需要追加分隔符
        currentTime = currentTime + "$_";
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warning("Unexpected exception from downstream: " + cause.getMessage());
        ctx.close();
    }
}
