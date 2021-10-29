package com.qingyun.network.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * @description： 处理IO操作的工具类
 * @author: 張青云
 * @create: 2021-10-27 21:45
 **/
public class IOUtil {

    /**
     * 从HTTP报文的输入流中读取请求头的一行
     * @param inputStream HTTP报文对应的输入流
     * @return 请求头中的一行，如果请求头结束则返回null
     */
    public static String readHttpLine(InputStream inputStream) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int ch;
        while ((ch = inputStream.read()) != -1) {
            if (ch == '\r') {
                //  再把\n读出来
                inputStream.read();
                break;
            }
            buffer.append((char) ch);
        }
        if (buffer.length() <= 0) {
            return null;
        } else {
            return buffer.toString();
        }
    }
}
