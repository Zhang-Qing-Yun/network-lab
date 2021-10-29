package com.qingyun.network.common.test;

import org.junit.Test;

import java.util.Arrays;
import java.util.Date;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-10-27 21:56
 **/
public class StringTest {

    @Test
    public void sbTest() {
        System.out.println(new StringBuilder().toString());
        System.out.println(new String(new char[]{}));
    }

    @Test
    public void splitTest() {
        String host = "127.0.0.1";
        System.out.println(host.split(":")[0]);
    }

    @Test
    public void timeTest() {
        String s = "Last-Modified: Fri, 12 May 2006 18:53:33 GMT";
        System.out.println(s.substring(15));
    }

    @Test
    public void byteAndStringTest() {
        String s = "hello world";
        StringBuffer buffer = new StringBuffer();
        byte[] bytes = s.getBytes();
        System.out.println(Arrays.toString(bytes));
        for (byte b: bytes) {
            buffer.append((char) b);
        }
        System.out.println(buffer.toString());
        System.out.println(Arrays.toString(buffer.toString().getBytes()));
    }

    @Test
    public void dataTest() {
        String md = "Thu, 28 Oct 2021 18:00:00 GMT";
        Date modified = new Date(md);
        String d = "Thu, 28 Oct 2021 17:00:00 GMT";
        Date date = new Date(d);

//        SimpleDateFormat dateformat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        time=dateformat1.format(date);
//        System.out.println(time);

        System.out.println(modified.compareTo(date) <= 0);
    }
}
