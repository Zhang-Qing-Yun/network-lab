package com.qingyun.network;

import com.qingyun.network.server.BIOProxyServer;
import com.qingyun.network.server.ProxyServer;

import java.io.IOException;

/**
 * @description： 启动类
 * @author: 張青云
 * @create: 2021-10-28 19:32
 **/
public class ProxyServerStart {
    public static void main(String[] args) {
        ProxyServer server = new BIOProxyServer(8888);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
