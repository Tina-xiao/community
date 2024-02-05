package com.nju.community.config;

import com.nju.community.controller.interceptor.AlphaInterceptor;
import com.nju.community.controller.interceptor.LoginRequiredInterceptor;
import com.nju.community.controller.interceptor.LoginTicketInterceptor;
import com.nju.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

//    @Autowired
//    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    //废弃掉登录拦截器，这里用spring security实现
//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        // **是指包含多级目录
//        registry.addInterceptor(alphaInterceptor)
//                //设置拦截器不拦截的资源
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg")
//                //设置拦截路径
//                .addPathPatterns("/register","/login");

        registry.addInterceptor(loginTicketInterceptor)
                //设置拦截器不拦截的资源
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

//        registry.addInterceptor(loginRequiredInterceptor)
//                //设置拦截器不拦截静态资源
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                //设置拦截器不拦截静态资源
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

    }
}
