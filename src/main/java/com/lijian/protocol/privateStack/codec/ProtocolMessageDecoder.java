package com.lijian.protocol.privateStack.codec;

import com.lijian.protocol.privateStack.message.Header;
import com.lijian.protocol.privateStack.message.PrivateProtocolMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Netty消息解码类
 * 1）继承LengthFieldBasedFrameDecoder（固定长度）就可以解决tcp的粘包和半包处理问题，
 * 2）只需要给我表示消息长度的字段偏移量和消息长度自身所占的字节数，该解码器就能自动实现对半包的处理；
 * 3）调用父类LengthFieldBasedFrameDecoder的decode方法后，返回的就是整包消息或者为null（说明半包）；
 **/
public class ProtocolMessageDecoder extends LengthFieldBasedFrameDecoder {
    public static final Logger log = LoggerFactory.getLogger(ProtocolMessageDecoder.class);

    PrivateProtocolMarshallingDecoder marshallingDecoder;

    public ProtocolMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
        marshallingDecoder = MarshallingCodecFactory.buildMarshallingDecoder();
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in1) throws Exception {
        // 父类LengthFieldBasedFrameDecoder解码后的消息，再对该消息体进行解码
        ByteBuf frame = (ByteBuf) super.decode(ctx, in1);
        if (frame == null) {
            return null;
        }
        PrivateProtocolMessage message = new PrivateProtocolMessage();
        Header header = new Header();
        header.setCrcCode(frame.readInt());
        header.setLength(frame.readInt());
        header.setSessionID(frame.readLong());
        header.setType(frame.readByte());
        header.setPriority(frame.readByte());
        int size = frame.readInt();
        if (size > 0) {
            int keySize = 0;
            byte[] keyArray = null;
            String key = null;
            Map<String, Object> attch = new HashMap<String, Object>();
            for (int i = 0; i < size; i++) {
                keySize = frame.readInt();
                keyArray = new byte[keySize];
                frame.readBytes(keyArray);
                key = new String(keyArray, "UTF-8");
                attch.put(key, marshallingDecoder.decode(ctx, frame));
            }
            header.setAttachment(attch);
        }
        //  readableBytes即为判断剩余可读取的字节数（ this.writerIndex - this.readerIndex）
        //  大于4说明有消息体（无消息体时readableBytes=4），故进行解码
        if (frame.readableBytes() > 4) {
            message.setBody(marshallingDecoder.decode(ctx, frame));
        }
        message.setHeader(header);
//        log.info("ProtocolMessageDecoder decode: {}", message);
        return message;
    }
}
