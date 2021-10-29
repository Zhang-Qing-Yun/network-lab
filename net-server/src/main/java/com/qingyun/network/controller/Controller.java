package com.qingyun.network.controller;


import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-10-28 20:47
 **/
@org.springframework.stereotype.Controller
public class Controller {
    int count = 0;
    String s = "Thu, 28 Oct 2021 18:00:00 GMT";

    @GetMapping("/*")
    public String getResource(HttpServletResponse response) {
        System.out.println(++count);
        response.setHeader("Last-Modified", s);
        return "index";
    }
}
