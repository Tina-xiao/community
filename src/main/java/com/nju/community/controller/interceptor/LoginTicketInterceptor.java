package com.nju.community.controller.interceptor;

import com.nju.community.entity.LoginTicket;
import com.nju.community.entity.User;
import com.nju.community.service.UserService;
import com.nju.community.util.CookieUtil;
import com.nju.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request,"ticket");
        //如果登陆了，就找到用户
        if(ticket!=null){
            //获取凭证
            LoginTicket loginTicket = userService.findLoginticket(ticket);
            //判断凭证是否有效
            if( loginTicket!=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
                //根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户，暂存用户，之后模板中要用
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
       //获取当前线程持有的user
        User user = hostHolder.getUser();
        if(user!=null && modelAndView!=null)
            modelAndView.addObject("loginUser",user);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
