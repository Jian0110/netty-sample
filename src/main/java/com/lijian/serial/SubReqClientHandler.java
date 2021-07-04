package com.lijian.serial;

import io.netty.channel.*;


/**
 * 订单请求Netty处理器
 */
@ChannelHandler.Sharable
public class SubReqClientHandler extends ChannelInboundHandlerAdapter {


    public SubReqClientHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 一次性发送10笔交易  验证是否粘包
        for (int i = 0; i < 10; i++) {
            ctx.write(subReq(i));
        }
        ctx.flush();
    }

    /**
     * 封装请求
     * @param i
     * @return
     */
    private SubscribeReq subReq(int i){
        SubscribeReq subscribeReq = new SubscribeReq();
        subscribeReq.setSubReqId(i);
        subscribeReq.setUserName("Lijian");
        subscribeReq.setProductName("Netty Book");
        subscribeReq.setPhoneNumber("138888888888");
        subscribeReq.setAddress("Yunnan Kunming");
        return subscribeReq;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 处理返回
        SubscribeResp res = (SubscribeResp) msg;
        System.out.println("Receive server response: ["+res.toString() + "]");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
