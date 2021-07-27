package com.lijian.bytebuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ByteBufferTest {

    private ByteBuffer buffer;

    public static void main(String[] args) {
    }

    public void ensureWritable() {
        int needSize = 1024;
        // 需要的空间比剩余的空间多，即剩余空间不足
        if (this.buffer.remaining() < needSize) {
            int toBeExtSize = needSize > 128 ? needSize : 128;
            // 需要创建一个新的ByteBuffer，并将之前的ByteBuffer复制到新创建的ByteBuffer中
            ByteBuffer tempBuffer = ByteBuffer.allocate(this.buffer.capacity() + toBeExtSize);
            this.buffer.flip();
            tempBuffer.put(this.buffer);
            this.buffer = tempBuffer;
        }
    }

    public void poolByteBuf(){
        ByteBuf poolBuffer = null;
        poolBuffer = PooledByteBufAllocator.DEFAULT.directBuffer(1024);
//        poolBuffer.writeBytes()
    }
}
