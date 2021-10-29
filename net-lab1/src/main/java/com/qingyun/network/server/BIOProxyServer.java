package com.qingyun.network.server;

import com.qingyun.network.factory.SingletonFactory;
import com.qingyun.network.task.ProxyTask;
import com.qingyun.network.util.ThreadUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @description： 基于BIO实现的代理服务器
 * @author: 張青云
 * @create: 2021-10-27 19:13
 **/
public class BIOProxyServer extends AbstractProxyServer {
    /**
     * 用于监听客户端连接的端口号
     */
    private final int port;

    /**
     * 用于具体执行客户端任务的线程池
     */
    private ExecutorService threadPool;


    public BIOProxyServer(int port) {
        this.port = port;

        //  创建线程池，并将其交给单例工厂来管理
        SingletonFactory singletonFactory = SingletonFactory.getInstance();
        threadPool = ThreadUtil.getMixedTargetThreadPool();
        try {
            singletonFactory.addSingleton(threadPool);
        } catch (Exception e) {
            //  do nothing
        }
    }

    @Override
    public void start() throws IOException {
        //  启动服务端
        ServerSocket serverSocket = new ServerSocket(port);
        startLog();
        //  遇到客户端连接就创建一个任务，然后提交到线程池当中，接下来由该线程与客户端保持通信
        while (true) {
            //  监听客户端连接
            Socket socket = serverSocket.accept();
            System.out.println("接收到了"+ socket.getInetAddress() + " " + socket.getPort() + "的连接");
            //  创建一个任务并提交给线程池处理
            threadPool.execute(new ProxyTask(socket));
        }
    }
}
