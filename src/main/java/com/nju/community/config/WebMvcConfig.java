package com.nju.community.config;

import com.nju.community.controller.interceptor.AlphaInterceptor;
import com.nju.community.controller.interceptor.LoginRequiredInterceptor;
import com.nju.community.controller.interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // **是指包含多级目录
        registry.addInterceptor(alphaInterceptor)
                //设置拦截器不拦截的资源
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jepg")
                //设置拦截路径
                .addPathPatterns("/register","/login");

        registry.addInterceptor(loginTicketInterceptor)
                //设置拦截器不拦截的资源
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jepg");

        registry.addInterceptor(loginRequiredInterceptor)
                //设置拦截器不拦截静态资源
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jepg");

    }
}
