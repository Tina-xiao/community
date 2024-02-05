package com.nju.community.controller.interceptor.advice;

import com.nju.community.util.CommunityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


import java.io.IOException;
import java.io.PrintWriter;

//表明该类是controller的全局配置类，且只处理带有controller注解的类
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    //表明这是处理异常的方法，括号里是处理什么异常,Exception是所有异常的父类，表明该方法处理所有异常
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常"+e.getMessage());
        for(StackTraceElement element :e.getStackTrace()){
            logger.error(element.toString());
        }

        //判断是返回页面/http的普通请求还是要传回json/html的异步请求
        String requestType =request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(requestType)){
            //异步
            response.setContentType("application/plain;charset=utf-8");//向浏览器返回普通字符串
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常！"));
        }
        else {
            response.sendRedirect(request.getContextPath()+ "/error");
        }

    }


}
