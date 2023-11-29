package com.nju.community.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.SecureRandom;

@Controller
@RequestMapping(path = "/session",method = RequestMethod.GET)
public class SessionController {

    //session示例
    @RequestMapping(path = "/set",method = RequestMethod.GET)
    @ResponseBody
    //session不像cookie需要手动创建，Spring mvc自动注入，类似于model，什么数据都能存储
    public String setSession(HttpSession session){
        session.setAttribute("id",1);
        session.setAttribute("name","Test");
        return "set Session";
    }

    @RequestMapping(path = "/get",method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){

        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get Session";
    }
}
