package com.lijian.protocol.privateStack.handler;

import com.lijian.protocol.privateStack.message.Header;
import com.lijian.protocol.privateStack.message.MessageType;
import com.lijian.protocol.privateStack.message.PrivateProtocolMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 登录认证请求处理器，即握手请求处理器
 */
public class LoginAuthReqHandler extends ChannelInboundHandlerAdapter {

    public static final Logger log = LoggerFactory.getLogger(LoginAuthReqHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //建立连接后，发送认证消息
        PrivateProtocolMessage message = buildLoginReq();
        log.info("client send login request：message={}", message);
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PrivateProtocolMessage message = (PrivateProtocolMessage) msg;
        // 若是握手应答消息，判断是否认证成功
        if (message.getHeader() != null && message.getHeader().getType() == MessageType.LOGIN_RESP) {
            byte loginResult = (byte) message.getBody();
            // 握手信息是没有消息体的
            if (loginResult != 0) {
                // 握手失败，关闭连接
                log.warn("login failed, close connection");
                ctx.close();
            } else {
                log.info("The client login success: message={}", message);
                ctx.fireChannelRead(msg);
            }

        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private PrivateProtocolMessage buildLoginReq() {
        PrivateProtocolMessage message = new PrivateProtocolMessage();
        Header header = new Header();
        header.setType((byte) MessageType.LOGIN_REQ);
        message.setHeader(header);
        return message;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
