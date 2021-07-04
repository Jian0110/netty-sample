package com.lijian.protocol.privateStack.handler;

import com.lijian.protocol.privateStack.message.Header;
import com.lijian.protocol.privateStack.message.MessageType;
import com.lijian.protocol.privateStack.message.PrivateProtocolMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录认证响应处理器：即握手应答响应
 */
public class LoginAuthRespHandler extends ChannelInboundHandlerAdapter {

    public static final Logger log = LoggerFactory.getLogger(LoginAuthRespHandler.class);

    private Map<String, Boolean> nodeCheck = new ConcurrentHashMap<>();

    /**
     * IP白名单
     */
    private String[] whiteList = {"127.0.0.1", "192.168.0.110"};

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        PrivateProtocolMessage message = (PrivateProtocolMessage) msg;
        // 若为握手认证消息，则校验并返回响应，否则传递到下一个handler
        if (message.getHeader() != null && message.getHeader().getType() == MessageType.LOGIN_REQ) {
            String nodeIndex = ctx.channel().remoteAddress().toString();
            PrivateProtocolMessage loginResp = null;
            // 防止重复登陆导致句柄泄露，所以拒绝
            if (nodeCheck.containsKey(nodeIndex)) {
                loginResp = buildResponse((byte) -1);
                log.info("repeat login, connection refused ：ip=" + nodeIndex);
            } else {
                boolean isOk = true;
                // 通过Channel接口获取客户端InetSocketAddress地址，从而获取发送发源地址
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = address.getAddress().getHostAddress();
                // 校验白名单
                for (String wip : whiteList) {
                    // 通过ip白名单
                    if (wip.equals(ip)) {
                        isOk = true;
                        nodeCheck.put(ip, true);
                        log.info("pass ip whitelist, ip=" + ip);
                        break;
                    }
                }
                // 构建握手应答信息返回客户端
                loginResp = isOk ? buildResponse((byte) 0) : buildResponse((byte) -1);
            }
            log.info("The login response is : " + loginResp + "[" + loginResp.getBody() + "]");
            ctx.writeAndFlush(loginResp);

        } else {
            ctx.fireChannelRead(msg);
        }

    }


    private PrivateProtocolMessage buildResponse(byte result) {
        PrivateProtocolMessage message = new PrivateProtocolMessage();
        Header header = new Header();
        header.setType(MessageType.LOGIN_RESP);
        message.setHeader(header);
        // body 0：认证成功  -1：认证失败
        message.setBody(result);
        return message;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 发生异常时关闭链路时，需要删除缓存，以保证下一次客户端能够成功登陆
        nodeCheck.remove(ctx.channel().remoteAddress().toString());
        ctx.close();
        ctx.fireExceptionCaught(cause);
    }
}
