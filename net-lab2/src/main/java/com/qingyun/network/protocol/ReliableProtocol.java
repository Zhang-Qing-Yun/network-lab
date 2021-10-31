package com.qingyun.network.protocol;

/**
 * @description： 可靠数据传输协议
 * @author: 張青云
 * @create: 2021-10-31 15:04
 **/
public interface ReliableProtocol {

    /**
     * 向目的主机发送数据
     * @param content 要发送的完整数据
     */
    void send(byte[] content) throws Exception;

    /**
     * 接收数据
     * @return 从目的主机接收到的完整数据
     */
    byte[] receive() throws Exception;
}
