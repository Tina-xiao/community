package com.nju.community.config;

import com.nju.community.service.UserService;
import com.nju.community.util.CommunityConstant;
import com.nju.community.util.CommunityUtil;
import com.nju.community.util.CookieUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@EnableWebSecurity
@Configuration
public class SecurityConfig  implements CommunityConstant {


    @Autowired
    private UserService userService;


    /**
     * 配置WebSecurity：注册WebSecurityCustomizer的一个实例
     * 对应：configure(WebSecurity)
     * 忽略对静态资源的拦截
     */
    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return new WebSecurityCustomizer() {
            @Override
            public void customize(WebSecurity web) {
                web.ignoring().requestMatchers("/resources/**");
            }
        };
    }

    /**
     * 配置过滤器链：注册SecurityFilterChain的一个实例
     *     对应： configure(HttpSecurity)
     *           对登录页面等进行配置，授权
     */

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //过滤请求
        http .authorizeHttpRequests()
                //以下必须登录请求
                .requestMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
)
                .hasAnyAuthority(
                        AUTHORITY_ADMIN,
                        AUTHORITY_USER,
                        AUTHORITY_MODERATOR
                )
                .requestMatchers(
                        "/discuss/top",
                        "/discuss/wonderful")
                .hasAnyAuthority(AUTHORITY_MODERATOR)
                .requestMatchers(
                        "/discuss/delete",
                        "/data/**")
                .hasAnyAuthority(AUTHORITY_ADMIN)
                //除了上面的请求之外其他请求全部放开，无需登录
                .anyRequest().permitAll();
                //.and().csrf().disable();

        //Spring Security 5 拦截器与SpringMVC拦截器的冲突导致无法授权问题解决
        //问题在于preHandle仅仅是在controller之前执行，而security验证权限的拦截器（底层由Filter实现）
        // UsernamePasswordAuthenticationFilter却更早执行，因此还没有对用户授权的时候就已经执行了验证权限的操作，这就是导致问题的原因。
        //在UsernamePasswordAuthenticationFilter之前新建一个Filter来代替preHandle中的授权操作，
        // 如下，在配置类SecurityConfig重写的protected void configure(HttpSecurity http)方法中增加一个Filter，
        // 该Filter在UsernamePasswordAuthenticationFilter之前执行，进行权限的验证以及授权。
        http.addFilterBefore(new Filter() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                //System.out.println("在这里！");
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                HttpServletResponse response = (HttpServletResponse) servletResponse;
                String ticket = CookieUtil.getValue(request,"ticket");
                CommunityUtil.setContext(ticket, userService);
                filterChain.doFilter(request,response);
            }
        }, UsernamePasswordAuthenticationFilter.class);

        // security底层对权限不够时的处理。异步请求，返回json，普通请求返回html
        http.exceptionHandling()
                //没登录提示信息
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        //判断为何种请求，同步还是异
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)) {
                            //异步请求，返回json
                            //希望返回XML类型，是异步请求
                            //application/plain代表返回普通字符串
                            response.setContentType("application/plain;charset=utf-8");
                            //字符流向前台输出
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登录！"));
                        } else {
                            //同步请求，返回登录页面
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                //授权失败,即登录了但是权限不足
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        //判断为何种请求，同步还是异步
                        String xRequestedWith = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)) {
                            //异步请求，返回json
                            //希望返回XML类型，是异步请求
                            //application/plain代表返回普通字符串
                            response.setContentType("application/plain;charset=utf-8");
                            //字符流向前台输出
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限！"));
                        } else {
                            //同步请求，返回错误页面
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });


                //退出登录提示信息
                //Security底层默认拦截/logout请求，进行退出处理
                //覆盖它默认的逻辑，才能执行我们自己的退出代码
                http.logout().logoutUrl("/securitylogout");

        return http.build();
    }



}
