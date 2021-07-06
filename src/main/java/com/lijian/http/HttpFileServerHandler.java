package com.lijian.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;


/**
 * Http文件服务处理器
 */
@ChannelHandler.Sharable
public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final String url;

    public HttpFileServerHandler(String url) {
        this.url = url;
    }

    protected void messageReceived(ChannelHandlerContext ctx,
                                   FullHttpRequest request) throws Exception {
        // 对HTTP请求消息解码结果进行判断
        if (!request.decoderResult().isSuccess()) {
            // 解码失败直接构造400错误
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        // 不是GET请求就返回405
        if (request.method() != HttpMethod.GET) {
            sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        final String uri = request.uri();
        final String path = sanitizeUri(uri);
        // 路径不合法，返回403错误
        if (path == null) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }
        // 使用URI路径构造file对象，如果是文件不存在或是隐藏文件就返回404
        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }
        // 如果是目录就发送目录的链接给客户端
        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                sendListing(ctx, file);
            } else {
                sendRedirect(ctx, uri + "/");
            }
            return;
        }
        // 文件合法性判断
        if (!file.isFile()) {
            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        RandomAccessFile randomAccessFile = null;
        try {
            // 以只读的方式打开文件，如果打开失败返回404错误
            randomAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }
        // 获取文件的长度构造成功的HTTP应答消息
        long fileLength = randomAccessFile.length();
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        HttpUtil.setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        // 判断是否KeepAlive，是设置响应头keepAlive
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);
        ChannelFuture sendFileFuture = null;
        // 通过Netty的ChunkedFile对象直接将文件写入到发送缓冲区中
        sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());
        // 发送文件添加监听器，发送完成后打印日志
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            @Override
            public void operationComplete(ChannelProgressiveFuture future)
                    throws Exception {
                System.out.println("Transfer complete.");
            }
            @Override
            public void operationProgressed(ChannelProgressiveFuture future,
                                            long progress, long total) throws Exception {
                if (total < 0) {
                    System.err.println("Transfer progress: " + progress);
                } else {
                    System.err.println("Transfer progress: " + progress + "/" + total);
                }
            }
        });

        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        // 如果是非keepAlive则最后消息发送完成后，服务器主动断开连接
        if (!HttpUtil.isKeepAlive(request)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    private String sanitizeUri(String uri) {
        try {
            // 对URI进行UTF-8解码
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                // 如果utf-8不支持，则尝试ISO-8859-1
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }
        // 解码成功后对uri进行合法性判断
        if (!uri.startsWith(url)) {
            return null;
        }
        if (!uri.startsWith("/")) {
            return null;
        }

        uri = uri.replace('/', File.separatorChar);
        if (uri.contains(File.separator + '.') || uri.contains('.' + File.separator) || uri.startsWith(".") || uri.endsWith(".")
                || INSECURE_URI.matcher(uri).matches()) {
            return null;
        }
        return System.getProperty("user.dir") + File.separator + uri;
    }

    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");


    /**
     * 发送目录的链接到客户端浏览器
     * @param ctx
     * @param dir 目录file
     */
    private static void sendListing(ChannelHandlerContext ctx, File dir) {
        // 创建Http响应
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        // 设置消息头类型为text/html
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");

        String dirPath = dir.getPath();
        StringBuilder buf = new StringBuilder();
        // 构造html页面内容
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append("目录:");
        buf.append("</title></head><body>\r\n");

        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        buf.append("<li>链接：<a href=\" ../\")..</a></li>\r\n");
        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }

        buf.append("</ul></body></html>\r\n");
        // 分配缓冲区
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        // 写入应答
        response.content().writeBytes(buffer);
        // 释放缓冲区
        buffer.release();
        // 将响应消息发送到缓冲区并刷新到SocketChannel中
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setContentTypeHeader(HttpResponse response, File file) {
        MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimetypesFileTypeMap.getContentType(file.getPath()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {

    }
}
