package com.lijian.protocol.privateStack.codec;

import com.lijian.protocol.privateStack.handler.HeartBeatReqHandler;
import com.lijian.protocol.privateStack.message.PrivateProtocolMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


/**
 * 消息编码器
 */
public class ProtocolMessageEncoder extends MessageToMessageEncoder<PrivateProtocolMessage> {

    public static final Logger log = LoggerFactory.getLogger(ProtocolMessageEncoder.class);
    private PrivateProtocolMarshallingEncoder marshallingEncoder;

    public ProtocolMessageEncoder() {
        marshallingEncoder = MarshallingCodecFactory.buildMarshallingEncoder();
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, PrivateProtocolMessage message, List<Object> out) throws Exception {
        if (message == null || message.getHeader() == null) {
            throw new Exception("The encode message is null");
        }
        ByteBuf buffer = Unpooled.buffer();
//        log.info("ProtocolMessageEncoder encode：" + message);
        // 按顺利编码后，根据定义的字段数据类型写入ByteBuf,解码时也要按顺序挨个取出
        buffer.writeInt(message.getHeader().getCrcCode());
        buffer.writeInt(message.getHeader().getLength());
        buffer.writeLong(message.getHeader().getSessionID());
        buffer.writeByte(message.getHeader().getType());
        buffer.writeByte(message.getHeader().getPriority());
        buffer.writeInt(message.getHeader().getAttachment().size());
        String key = null;
        Object value = null;
        byte[] keyArray = null;
        //针对header中的附件编码
        for (Map.Entry<String, Object> param : message.getHeader().getAttachment().entrySet()) {
            key = param.getKey();
            keyArray = key.getBytes("UTF-8");
            value = param.getValue();

            buffer.writeInt(keyArray.length);
            buffer.writeBytes(keyArray);
            marshallingEncoder.encode(channelHandlerContext, value, buffer);

        }
        if (message.getBody() != null) {
            //使用MarshallingEncoder编码消息体
            marshallingEncoder.encode(channelHandlerContext, message.getBody(), buffer);
        } else {
            //没有消息体的话，就赋予0值
            buffer.writeInt(0);
        }
        //更新消息长度字段的值，至于为什么-8，是因为8是长度字段后的偏移量，LengthFieldBasedFrameDecoder的源码中
        //对长度字段和长度的偏移量之和做了判断，如果不-8，会导致LengthFieldBasedFrameDecoder解码返回null
        //这是 《Netty权威指南》中的写错的地方
        buffer.setInt(4, buffer.readableBytes() - 8);
        //书中此处没有add，也即没有将ByteBuf加入到List中，也就没有消息进行编码了，所以导致运行了没有效果……
        out.add(buffer);
    }
}
