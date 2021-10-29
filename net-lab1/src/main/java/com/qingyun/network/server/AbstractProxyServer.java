package com.qingyun.network.server;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-10-28 12:35
 **/
public abstract class AbstractProxyServer implements ProxyServer {
    /**
     * 启动成功时的打印日志
     */
    protected void startLog() {
        System.out.println("启动代理服务器成功");
    }

    /**
     * 关闭代理服务器完成时的打印日志
     */
    protected void closeLog() {
        System.out.println("关闭代理服务器完成");
    }
}
