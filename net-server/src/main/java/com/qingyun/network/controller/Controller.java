package com.qingyun.network.controller;


import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @description：
 * @author: 張青云
 * @create: 2021-10-28 20:47
 **/
@org.springframework.stereotype.Controller
public class Controller {
    String s = "Thu, 28 Oct 2021 18:00:00 GMT";
    Date modified = new Date(s);

    @GetMapping("/*")
    public String getResource(HttpServletRequest request, HttpServletResponse response) {
        String since = request.getHeader("If-modified-since");
        if (since != null) {
            //  比较两个日期
            Date date = new Date(since);
            if (modified.compareTo(date) <= 0) {
                response.setStatus(304);
                return null;
            }
        }
        response.setHeader("Last-Modified", s);
        return "index";
    }
}
