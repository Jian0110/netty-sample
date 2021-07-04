package com.lijian.serial;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


/**
 * 订单订阅Netty处理器
 */
@ChannelHandler.Sharable
public class SubReqServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 处理请求
        SubscribeReq req = (SubscribeReq) msg;
        if ("Lijian".equalsIgnoreCase(req.getUserName())) {
            System.out.println("Service accept client subscribe req : ["
                    + req.toString() + "]");
            // 返回结果
            ctx.writeAndFlush(resp(req.getSubReqId()));
        }

    }


    /**
     * 封装返回
     * @param subReqID
     * @return
     */
    private SubscribeResp resp(int subReqID) {
        SubscribeResp resp = new SubscribeResp();
        resp.setSubReqId(subReqID);
        resp.setRespCode("0");
        resp.setDesc("Netty book order succeed, 3 days later, sent to the designated address");
        return resp;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
