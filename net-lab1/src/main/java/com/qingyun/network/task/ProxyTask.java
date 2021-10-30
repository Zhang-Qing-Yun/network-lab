package com.qingyun.network.task;

import com.google.common.primitives.Bytes;
import com.qingyun.network.cache.LRUCache;
import com.qingyun.network.config.ProxyConfig;
import com.qingyun.network.constants.ProxyConstants;
import com.qingyun.network.factory.SingletonFactory;
import com.qingyun.network.util.IOUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @description： 具体执行代理业务的任务，目前只能做到一次请求一个TCP连接即HTTP1.0的情景
 * @author: 張青云
 * @create: 2021-10-27 20:07
 **/
public class ProxyTask implements Runnable {
    //  用于和客户端通信的TCP套接字
    private final Socket socket;

    //  用户和目的服务器建立连接的TCP套接字
    private Socket targetSocket;

    //  缓存
    private final LRUCache cache;

    //  配置信息
    private final ProxyConfig config;

    public ProxyTask(Socket socket) {
        this.socket = socket;
        cache = SingletonFactory.getInstance().getObject(LRUCache.class);
        config = SingletonFactory.getInstance().getObject(ProxyConfig.class);
    }

    @Override
    public void run() {
        InputStream clientInputStream;
        OutputStream clientOutputStream;
        String url = null;
        String host = null;
        int port = 80;
        StringBuffer buffer = new StringBuffer();  // HTTP请求头的字符形式

        try {
            clientInputStream = socket.getInputStream();
            clientOutputStream = socket.getOutputStream();

            //  解析HTTP请求头
            String line;
            while ((line = IOUtil.readHttpLine(clientInputStream)) != null) {
                if (line.startsWith("GET")) {
                    //  GET /index.html HTTP1.1
                    url = line.split(" ")[1];
                } else if (line.startsWith("Host")) {
                    //  Host: 127.0.0.1:80
                    host = line.split(" ")[1];
                }
                buffer.append(line).append("\r\n");
            }
            buffer.append("\r\n");

            if (host == null) {
                //  TODO：处理没有带host字段的请求
                return;
            }

            //  解析地址和端口号，如果没有端口号则使用默认的80
            String[] split = host.split(":");
            host = split[0];
            if (split.length != 1) {
                port = Integer.parseInt(split[1]);
            }

            //  网站过滤
            if (config.getUrlRule() == ProxyConstants.ALLOW_URL) {
                //  如果当前访问的网站没在配置文件中则拦截
                if (!config.getUrls().contains(host)) {
                    clientOutputStream.write(refuseProxy().getBytes());
                    clientOutputStream.flush();
                    return;
                }
            } else if (config.getUrlRule() == ProxyConstants.REFUSE_URL) {
                //  如果要访问的网站存在于配置文件中则拦截
                if (config.getUrls().contains(host)) {
                    clientOutputStream.write(refuseProxy().getBytes());
                    clientOutputStream.flush();
                    return;
                }
            } else {  // 配置文件写错了
                clientOutputStream.write(refuseProxy().getBytes());
                clientOutputStream.flush();
                return;
            }

            //  用户过滤
            String clientHost = socket.getInetAddress().getHostAddress();
            if (config.getUserRule() == ProxyConstants.ALLOW_USER) {
                //  如果客户端的Host不在配置文件里拦截
                if (config.getUsers().contains(clientHost)) {
                    clientOutputStream.write(refuseProxy().getBytes());
                    clientOutputStream.flush();
                    return;
                }
            } else if (config.getUserRule() == ProxyConstants.REFUSE_USER) {
                //  如果客户端的Host在配置文件里拦截
                if (config.getUsers().contains(clientHost)) {
                    clientOutputStream.write(refuseProxy().getBytes());
                    clientOutputStream.flush();
                    return;
                }
            } else {  // 配置文件写错了
                clientOutputStream.write(refuseProxy().getBytes());
                clientOutputStream.flush();
                return;
            }

            //  钓鱼
            if (config.getFishingUsers().contains(clientHost)) {
                //  构造发送给钓鱼网站的HTTP报文
                StringBuffer fishingHTTP = new StringBuffer();
                fishingHTTP.append("GET " + ProxyConstants.fishingUrl + " HTTP/1.1" + "\r\n");
                fishingHTTP.append("Host: " + ProxyConstants.fishingHost + "\r\n");
                fishingHTTP.append("\r\n");
                String fishingHTTPStr = fishingHTTP.toString();

                //  建立连接然后发送数据
                String[] hostAndPort = ProxyConstants.fishingHost.split(":");
                targetSocket = new Socket(hostAndPort[0], Integer.parseInt(hostAndPort[1]));
                OutputStream outputStream = targetSocket.getOutputStream();
                outputStream.write(fishingHTTPStr.getBytes());
                waitTargetServerAndTransfer(clientOutputStream, targetSocket.getInputStream());
                return;
            }

            //  对于非GET请求的方法，直接转发给目的服务器
            if (url == null) {
                transfer(host, port, buffer, clientInputStream, clientOutputStream);
                return;
            }

            String uri = url;
            byte[] content = cache.getContent(uri);
            //  对于GET方法，如果缓存中存在则向目的服务器发送条件GET
            if (content != null) {
                //  从缓存中提取Last-Modified值
                String lastModified = parseLastModified(content);
                //  构造条件GET请求
                StringBuffer ifGetReqBuffer = new StringBuffer();
                ifGetReqBuffer.append("GET " + url + " HTTP/1.1\r\n");
                ifGetReqBuffer.append("Host: " + host + ":" + port + "\r\n");
                ifGetReqBuffer.append("If-modified-since: " + lastModified + "\r\n");
                ifGetReqBuffer.append("\r\n");
                String ifGetReq = ifGetReqBuffer.toString();
                //  向目的服务器发送
                targetSocket = new Socket(host, port);
                OutputStream outputStream = targetSocket.getOutputStream();
                InputStream inputStream = targetSocket.getInputStream();
                outputStream.write(ifGetReq.getBytes());
                outputStream.flush();
                //  阻塞式监听目的服务器的返回值
                String respFirstLine = IOUtil.readHttpLine(inputStream);
                int code = Integer.parseInt(respFirstLine.split(" ")[1]);
                //  缓存过期
                if (code != 304) {
                    System.out.println("代理服务器对" + uri + "的缓存过期");
                    //  将报文转发至客户端
                    byte[] firstLine = (respFirstLine + "\r\n").getBytes();
                    clientOutputStream.write(firstLine);
                    byte[] resp = waitTargetServerAndTransfer(clientOutputStream, inputStream);
                    //  将响应结果进行缓存，只缓存具有Last-Modified首部行的响应结果
                    if (new String(resp).contains("Last-Modified")) {
                        cache.addCache(uri, ArrayUtils.addAll(firstLine, resp));
                        System.out.println("对" + uri + "的响应结果进行了缓存");
                    }
                } else {  // 缓存命中
                    System.out.println("对" + uri + "的访问命中缓存");
                    //  直接返回缓存中的值
                    clientOutputStream.write(content);
                    clientOutputStream.flush();
                }
            } else { //  缓存不存在，则直接请求目的服务器，然后转发给客户端，并在代理服务器进行缓存
                System.out.println("代理服务器没有对" + uri + "请求的缓存");
                byte[] resp = transfer(host, port, buffer, null, clientOutputStream);
                //  将响应结果进行缓存，只缓存具有Last-Modified首部行的响应结果
                if (new String(resp).contains("Last-Modified")) {
                    cache.addCache(uri, resp);
                    System.out.println("对" + uri + "的响应结果进行了缓存");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
                if (targetSocket != null) {
                    targetSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 在客户端和目标服务器之间进行转发，也就是将客户端的内容直接发送到目的服务器，然后再将目的服务器返回的内容直接转发给客户端
     * @param host 目标服务器主机地址
     * @param port 目标服务器端口号
     * @param head HTTP的请求头
     * @param body HTTP除去head后的内容的输入流
     * @param clientOutputStream 客户端socket的输出流
     * @return 目标服务器返回的相应内容
     */
    private byte[] transfer(String host, int port, StringBuffer head, InputStream body, OutputStream clientOutputStream) throws IOException {
        //  和远程服务器建立连接
        //  TODO：有BUG，可能连不上目标服务器
        targetSocket = new Socket(host, port);
        InputStream targetServerInputStream = targetSocket.getInputStream();
        OutputStream targetServerOutputStream = targetSocket.getOutputStream();

        //  先写入请求头
        targetServerOutputStream.write(head.toString().getBytes());
        //  请求体不为null时写入请求体
        if (body != null) {
            byte[] bytes = new byte[256 * 1024];
            int size;
            // TODO：有BUG，可能读不到完整数据；但是如果while循环读的话，如果目标服务器不关闭TCP连接，则会阻塞在这里
            if ((size = body.read(bytes)) >= 0) {
                targetServerOutputStream.write(bytes, 0, size);
            }
        }
        targetServerOutputStream.flush();

        //  同步阻塞式等待目标服务器返回响应
        return waitTargetServerAndTransfer(clientOutputStream, targetServerInputStream);
    }

    /**
     * 同步阻塞式等待目标服务器返回响应，并且将响应结果直接返回给客户端
     * @param clientOutputStream 到客户端的输出流
     * @param targetServerInputStream 到目的服务器的输入流
     * @return 客户端的响应结果
     */
    private byte[] waitTargetServerAndTransfer(OutputStream clientOutputStream,
                                               InputStream targetServerInputStream) throws IOException {
        List<byte[]> response = new ArrayList<>();
        byte[] bytes = new byte[256 * 1024];
        int length;
        // TODO：有BUG，可能读不到完整数据；但是如果while循环读的话，如果目标服务器不关闭TCP连接，则会阻塞在这里
        if ((length = targetServerInputStream.read(bytes)) >= 0) {
            //  写回给客户端
            clientOutputStream.write(bytes, 0, length);
            //  收集响应结果
            byte[] part = new byte[length];
            System.arraycopy(bytes, 0, part, 0, length);
            response.add(part);
        }

        //  将响应结果返回
        List<Byte> list = new LinkedList<>();
        for (byte[] one: response) {
            list.addAll(Bytes.asList(one));
        }
        return Bytes.toArray(list);
    }

    /**
     * 从缓存中提取Last-Modified值
     * @param context HTTP报文
     * @return Last-Modified值，如果没有则返回null
     */
    private String parseLastModified(byte[] context) {
        StringBuffer headLine = new StringBuffer();
        for (int i = 0; i < context.length; i++) {
            if (context[i] == '\r') {
                //  请求头解析结束时都没有找到Last-Modified请求行
                if (headLine.length() == 0) {
                    return null;
                }
                String s = headLine.toString();
                if (s.startsWith("Last-Modified")) {
                    return s.substring(15);
                }
                i++;
                headLine = new StringBuffer();
                continue;
            }
            headLine.append((char) context[i]);
        }
        return null;
    }

    /**
     * 拒绝代理时向客户端返回的HTTP报文
     */
    private String refuseProxy() {
        return null;
    }
}
