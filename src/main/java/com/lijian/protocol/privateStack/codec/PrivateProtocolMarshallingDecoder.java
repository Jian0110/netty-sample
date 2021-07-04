package com.lijian.protocol.privateStack.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.UnmarshallerProvider;


/**
 * 私有协议解码器
 * JBoss
 */
public class PrivateProtocolMarshallingDecoder extends MarshallingDecoder {
    public PrivateProtocolMarshallingDecoder(UnmarshallerProvider provider) {
        super(provider);
    }

    public PrivateProtocolMarshallingDecoder(UnmarshallerProvider provider, int maxObjectSize) {
        super(provider, maxObjectSize);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        return super.decode(ctx, in);
    }
}
