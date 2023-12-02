package com.nju.community.controller.interceptor;

import com.nju.community.annotation.LoginRequired;
import com.nju.community.entity.User;
import com.nju.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //只拦截被@LoginRequired标注的方法,不拦截其他资源，包括静态资源,handler是被拦截的对象
        //instanceof测试它左边的对象是否是它右边的类的实例
        if(handler instanceof HandlerMethod){
            //转型
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //获取拦截到的method对象
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            if(loginRequired!=null && hostHolder.getUser()==null){
                //通过response重定向
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
            return true;
        }

        return true;
    }
}
