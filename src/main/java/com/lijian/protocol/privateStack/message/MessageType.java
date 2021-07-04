package com.lijian.protocol.privateStack.message;

public class MessageType {

    public static final byte LOGIN_R2=0;//业务请求消息
    public static final byte LOGIN_1=1;//业务响应消息
    public static final byte LOGIN_2=2;//业务ONE WAY 消息
    public static final byte LOGIN_REQ=3;//握手请求消息
    public static final byte LOGIN_RESP=4;//握手应答消息
    public static final byte HEARTBEAT_REQ=5;//心跳请求消息
    public static final byte HEARTBEAT_RESP=6;//心跳应答消息
}
