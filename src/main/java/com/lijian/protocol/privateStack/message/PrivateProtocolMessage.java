package com.lijian.protocol.privateStack.message;

public class PrivateProtocolMessage {

    /**
     * message header
     */
    private Header header;

    /**
     * message body
     */
    private Object body;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "PrivateProtocolMessage{" +
                "header=" + header +
                ", body=" + body +
                '}';
    }
}
