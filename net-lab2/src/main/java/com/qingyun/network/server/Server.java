package com.qingyun.network.server;

import com.qingyun.network.client.Client;
import com.qingyun.network.protocol.GBN;
import com.qingyun.network.protocol.ReliableProtocol;
import com.qingyun.network.protocol.SR;

import java.io.*;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-10-31 20:38
 **/
public class Server {
    public static void main(String[] args) throws Exception {
        //  获取类路径
        String path = Client.class.getResource("/").toURI().getPath();
        File file1 = new File(path + "2.webp");
        File file2 = new File(path + "3.webp");
        if(!file1.exists()) {
            if(!file1.createNewFile()) {
                System.out.println("创建文件失败！");
                return;
            }
        }
        ReliableProtocol server = new GBN("127.0.0.1", 8080, 7070);
//        ReliableProtocol server = new SR("127.0.0.1", 8080, 7070);
        System.out.println("开始从 127.0.0.1:8080 处接收1.webp");
        ByteArrayOutputStream byteArrayOutputStream;
        byte[] receive = server.receive();
        if(receive.length != 0) {
            FileOutputStream fileOutputStream = new FileOutputStream(file1);
            fileOutputStream.write(receive, 0, receive.length);
            fileOutputStream.close();
            System.out.println("获取文件1.png完成\n存为2.webp");
        }
        byteArrayOutputStream = new ByteArrayOutputStream();
        Client.cloneStream(byteArrayOutputStream, new FileInputStream(file2));
        System.out.println("开始向 127.0.0.1:7070 发送3.webp");
        server.send(byteArrayOutputStream.toByteArray());
    }
}
