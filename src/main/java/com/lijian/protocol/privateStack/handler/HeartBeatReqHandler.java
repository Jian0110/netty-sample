package com.lijian.protocol.privateStack.handler;

import com.lijian.protocol.privateStack.message.Header;
import com.lijian.protocol.privateStack.message.MessageType;
import com.lijian.protocol.privateStack.message.PrivateProtocolMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 客户端心跳检测处理器：
 * 握手成功后由客户端主动发送心跳消息
 * 服务端接接收到心跳消息后，返回心跳应答消息
 */
public class HeartBeatReqHandler extends ChannelInboundHandlerAdapter {

    public static final Logger log = LoggerFactory.getLogger(HeartBeatReqHandler.class);
    /**
     * 定时器任务，用来定时发送心跳信息
     */
    private volatile ScheduledFuture<?> heartBeat;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PrivateProtocolMessage message = (PrivateProtocolMessage) msg;
        // 认证成功后，定时发送心跳检测
        if (message.getHeader() != null && message.getHeader().getType() == MessageType.LOGIN_RESP) {
            // 默认每5s发送一次心跳信息
            heartBeat = ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx), 0, 5000, TimeUnit.MILLISECONDS);
        } else if (message.getHeader() != null && message.getHeader().getType() == MessageType.HEARTBEAT_RESP) {
            log.info("Client receive server heart beat message : --->{}", message);
        } else {
            // 传递给下一个Handler处理
            ctx.fireChannelRead(msg);
        }
    }


    private class HeartBeatTask implements Runnable {
        ChannelHandlerContext ctx;
        /**
         * 通过构造函数获取ChannelHandlerContext，构造心跳信息并发送
         * @param ctx
         */
        public HeartBeatTask(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }
        @Override
        public void run() {
            PrivateProtocolMessage message = buildHeatBeat();
            log.info("Client send heart beat message to server :  --->{}", message);
            ctx.writeAndFlush(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 链路异常取消心跳检测
        if (heartBeat != null) {
            heartBeat.cancel(true);
            heartBeat = null;
        }
        ctx.fireExceptionCaught(cause);
    }


    private PrivateProtocolMessage buildHeatBeat() {
        PrivateProtocolMessage message = new PrivateProtocolMessage();
        Header header = new Header();
        // 心跳检测仅消息头就够了
        header.setType(MessageType.HEARTBEAT_REQ);
        message.setHeader(header);
        return message;
    }
}
