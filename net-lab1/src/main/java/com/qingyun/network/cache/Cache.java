package com.qingyun.network.cache;

/**
 * @description： 用于缓存一些访问请求及其响应结果
 * @author: 張青云
 * @create: 2021-10-27 20:18
 **/
public interface Cache {
    /**
     * 向缓存中添加一个请求的响应结果
     * @param url 请求的路径
     * @param content 该请求的响应结果
     */
    void addCache(String url, byte[] content);

    /**
     * 从代理服务器的缓存中获取某个请求的响应结果
     * @param url 请求路径
     * @return 响应结果
     */
    byte[] getContent(String url);
}
