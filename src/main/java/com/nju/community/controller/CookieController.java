package com.nju.community.controller;

import com.nju.community.util.CommunityUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cookie")
public class CookieController {

    //浏览器第一次访问服务器，服务器保存在浏览器端的cookie
    @RequestMapping(value = "/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //创建cookie,一个cookie只能存一对key-value，且只能存字符串
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie请求范围
        cookie.setPath("/community/cookie");
        //设置超时时间
        cookie.setMaxAge(60*10);
        response.addCookie(cookie);
        return "set cookie";
    }

    //浏览器下次访问该服务器时，自动携带该cookie,浏览器的检查可以看到cookie信息
    @RequestMapping(path = "get",method = RequestMethod.GET)
    @ResponseBody
    //将key为code的cookie值赋给code
    public String getCookie(@CookieValue("code") String code){
         System.out.println(code);
        return "get cookie";
    }

}
