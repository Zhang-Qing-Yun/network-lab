package com.qingyun.network.client;

import com.qingyun.network.protocol.GBN;
import com.qingyun.network.protocol.ReliableProtocol;
import com.qingyun.network.protocol.SR;

import java.io.*;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-10-31 20:38
 **/
public class Client {
    public static void main(String[] args) throws Exception {
        //  获取类路径
        String path = Client.class.getResource("/").toURI().getPath();
        File file1 = new File(path + "1.webp");
        File file2 = new File(path + "4.webp");
        if(!file2.exists()) {
            if(!file2.createNewFile()) {
                System.out.println("创建文件失败！");
                return;
            }
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        cloneStream(byteArrayOutputStream, new FileInputStream(file1));
//        ReliableProtocol client = new GBN("127.0.0.1", 7070, 8080);
        ReliableProtocol client = new SR("127.0.0.1", 7070, 8080);
        System.out.println("开始向 127.0.0.1:7070 发送1.webp");
        client.send(byteArrayOutputStream.toByteArray());

        System.out.println("开始从 127.0.0.1:7070 处接收3.webp");
        Thread.sleep(50);
        byte[] resp = client.receive();
        if(resp.length != 0) {
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            fileOutputStream.write(resp);
            fileOutputStream.close();
            System.out.println("获取文件3.png完成\n存为4.webp");
        }
    }

    public static void cloneStream(ByteArrayOutputStream res, InputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while((length = in.read(buffer)) >= 0) {
            res.write(buffer, 0, length);
        }
        res.flush();
    }
}
