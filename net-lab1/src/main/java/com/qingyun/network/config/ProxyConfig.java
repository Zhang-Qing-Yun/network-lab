package com.qingyun.network.config;

import lombok.Getter;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @description： 代理服务器的配置信息
 * @author: 張青云
 * @create: 2021-10-29 21:01
 **/
@Getter
@ToString
public class ProxyConfig {
    //  对url的放行模式：1为只对配置的url放行，0为只拒绝配置的url
    private int urlRule = 0;

    //  配置的url
    private final Set<String> urls = new HashSet<>();

    //  对User（也就是ip地址）的放行规则
    private int userRule = 0;

    //  配置的User
    private final Set<String> users = new HashSet<>();

    //  要被钓鱼的User
    private final Set<String> fishingUsers = new HashSet<>();

    public void setUrlRule(int urlRule) {
        this.urlRule = urlRule;
    }

    public void setUserRule(int userRule) {
        this.userRule = userRule;
    }

    public void setUrls(List<String> urls) {
        for (String url: urls) {
            this.urls.add(url);
        }
    }

    public void setUsers(List<String> users) {
        for (String user: users) {
            this.users.add(user);
        }
    }

    public void setFishingUsers(List<String> fishingUsers) {
        for (String fishingUser: fishingUsers) {
            this.fishingUsers.add(fishingUser);
        }
    }
}
