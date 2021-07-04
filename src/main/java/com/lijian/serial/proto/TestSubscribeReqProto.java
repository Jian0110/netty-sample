package com.lijian.serial.proto;

import com.google.protobuf.InvalidProtocolBufferException;
import com.lijian.protobuf.SubscribeReqProto;

public class TestSubscribeReqProto {


    /**
     * 编码为byte数组
     * @param req
     * @return
     */
    private static byte[] encode(SubscribeReqProto.SubscribeReq req){
        return req.toByteArray();
    }


    /**
     * 解码：通过parseFrom将byte[]数组解码为原始的对象
     * @param body
     * @return
     * @throws InvalidProtocolBufferException
     */
    private static SubscribeReqProto.SubscribeReq decode(byte[] body) throws InvalidProtocolBufferException {
        return SubscribeReqProto.SubscribeReq.parseFrom(body);
    }

    private static  SubscribeReqProto.SubscribeReq createSubscribeReq(){
        SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
        builder.setSubReqId(1);
        builder.setUserName("Lijian");
        builder.setProductName("Netty Book");
        builder.setAddress("Yunnan Kunming");
        return builder.build();
    }

    public static void main(String[] args) throws InvalidProtocolBufferException {
        SubscribeReqProto.SubscribeReq req = createSubscribeReq();
        System.out.println("Before encode: " + req.toString());
        SubscribeReqProto.SubscribeReq req2 = decode(encode(req));
        System.out.println("After decode: " + req.toString());
        System.out.println("Assert equal: --> " + req.equals(req2));
    }


}
