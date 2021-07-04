package com.lijian.protocol.privateStack.handler;

import com.lijian.protocol.privateStack.message.Header;
import com.lijian.protocol.privateStack.message.MessageType;
import com.lijian.protocol.privateStack.message.PrivateProtocolMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

/**
 * 服务端心跳检测处理器
 */
public class HeartBeatRespHandler extends ChannelInboundHandlerAdapter {

    public static final Logger log = LoggerFactory.getLogger(HeartBeatRespHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PrivateProtocolMessage message = (PrivateProtocolMessage) msg;
        // 收到心跳消息后，构造心跳应答消息返回
        if (message.getHeader() != null && message.getHeader().getType() == MessageType.HEARTBEAT_REQ) {
            log.info("receive client heart beat message : ---> {}", message);
            PrivateProtocolMessage heartBeat = buildHeatBeat();
            log.info("send heart beat response message to client : ---> {}", heartBeat);
            ctx.writeAndFlush(heartBeat);
        }  else {
            // 传递给下一个handler处理
            ctx.fireChannelRead(msg);
        }

    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 服务端连续几次没有收到ping心跳，则关闭链路，释放资源，等待客户端重连
        ctx.fireExceptionCaught(cause);
    }

    private PrivateProtocolMessage buildHeatBeat() {
        PrivateProtocolMessage message = new PrivateProtocolMessage();
        Header header = new Header();
        header.setType(MessageType.HEARTBEAT_RESP);
        message.setHeader(header);
        return message;
    }
}
