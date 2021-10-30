package com.qingyun.network.server;

import com.qingyun.network.config.ProxyConfig;
import com.qingyun.network.factory.SingletonFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-10-28 12:35
 **/
public abstract class AbstractProxyServer implements ProxyServer {
    /**
     * 加载配置信息
     */
    protected void initConfig() {
        InputStream inputStream = null;
        BufferedReader urlReader = null;
        BufferedReader userReader = null;
        BufferedReader fishingReader = null;

        try {
            //  读取主配置文件proxy.properties
            inputStream = this.getClass().getClassLoader().getResourceAsStream("proxy.properties");
            Properties properties = new Properties();
            properties.load(inputStream);

            //  加载主配置
            SingletonFactory factory = SingletonFactory.getInstance();
            ProxyConfig config = factory.getObject(ProxyConfig.class);
            config.setUrlRule(Integer.parseInt(properties.getProperty("urlRule")));
            config.setUserRule(Integer.parseInt(properties.getProperty("userRule")));

            //  设置配置的url，文件里一行就是一个url
            InputStream urlStream = this.getClass().getClassLoader().getResourceAsStream("url.txt");
            if (urlStream != null) {
                urlReader = new BufferedReader(new InputStreamReader(urlStream));
                List<String> urls = new ArrayList<>();
                String line;
                while ((line = urlReader.readLine()) != null) {
                    urls.add(line);
                }
                config.setUrls(urls);
            }

            //  设置配置的User即主机地址，一行就是一个地址
            InputStream userStream = this.getClass().getClassLoader().getResourceAsStream("user.txt");
            if (userStream != null) {
                userReader = new BufferedReader(new InputStreamReader(userStream));
                List<String> users = new ArrayList<>();
                String line;
                while ((line = userReader.readLine()) != null) {
                    users.add(line);
                }
                config.setUsers(users);
            }

            //  设置要被钓鱼的用户，一行就是就是一个用户即主机地址
            InputStream fishingStream = this.getClass().getClassLoader().getResourceAsStream("fishing.txt");
            if (fishingStream != null) {
                fishingReader = new BufferedReader(new InputStreamReader(fishingStream));
                List<String> fishingUsers = new ArrayList<>();
                String line;
                while ((line = fishingReader.readLine()) != null) {
                    fishingUsers.add(line);
                }
                config.setFishingUsers(fishingUsers);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("配置文件不存在或格式不正确");
        } finally {
            //  关闭资源
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlReader != null) {
                try {
                    urlReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (userReader != null) {
                try {
                    userReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fishingReader != null) {
                try {
                    fishingReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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
