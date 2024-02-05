package com.nju.community.controller.interceptor;

import com.nju.community.entity.LoginTicket;
import com.nju.community.entity.User;
import com.nju.community.service.UserService;
import com.nju.community.util.CookieUtil;
import com.nju.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Component
@EnableWebSecurity
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    // 持久化权限，解决权限不生效问题
    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request,"ticket");
        //如果登陆了，就找到用户
        if(ticket!=null){
            //获取凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //判断凭证是否有效
            if( loginTicket!=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
                //根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户，暂存用户，之后模板中要用
                hostHolder.setUser(user);
                //构建用户认证的结果，并存入SecurityContext，以便于Security进行授权
                // principal: 主要信息; credentials: 证书; authorities: 权限; UsernamePasswordAuthenticationToken这个方法只与账号密码有关，验证码认证有别的类
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId()));
                //存入Context
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
                securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
               // System.out.println("用户认证信息！"+authentication.toString());
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
        SecurityContextHolder.clearContext();
    }
}
