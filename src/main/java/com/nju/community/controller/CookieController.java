package com.nju.community.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.server.Cookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CookieController {
    @RequestMapping(value = "/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public void setCookie(HttpServletResponse response){
        //创建cookie
//        Cookie cookie = new Cookie("code",CommunityUtil.generateUUID());

    }
}
