package com.qingyun.network.constants;

/**
 * @description： 代理服务器的常量
 * @author: 張青云
 * @create: 2021-10-29 21:03
 **/
public interface ProxyConstants {
    /**
     * 代理服务器访问地址配置为允许访问模式
     */
    int ALLOW_URL = 1;

    /**
     * 代理服务器访问地址配置为拒绝访问模式
     */
    int REFUSE_URL = 0;

    /**
     * 允许某些用户访问外部地址的模式
     */
    int ALLOW_USER = 1;

    /**
     * 禁止某些用户访问外部地址的模式
     */
    int REFUSE_USER = 0;

    /**
     * 钓鱼网站的地址
     */
    String fishingHost = "8.140.166.139:9999";

    /**
     * 钓鱼地址
     */
    String fishingUrl = "/hh";
}
