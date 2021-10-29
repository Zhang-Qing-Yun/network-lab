package com.qingyun.network.server;

import java.io.IOException;

/**
 * @description： 代理服务器
 * @author: 張青云
 * @create: 2021-10-27 19:12
 **/
public interface ProxyServer {

    /**
     * 启动代理服务器
     * @throws IOException 启动时发生的异常
     */
    void start() throws IOException;
}
