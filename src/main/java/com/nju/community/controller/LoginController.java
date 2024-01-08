package com.nju.community.controller;


import com.google.code.kaptcha.Producer;
import com.nju.community.entity.User;
import com.nju.community.service.UserService;
import com.nju.community.util.CommunityConstant;
import com.nju.community.util.CommunityUtil;
import com.nju.community.util.RedisKeyUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    //以当前类名为命名的logger
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    //访问注册页面
    @RequestMapping(value = "/register",method = RequestMethod.GET)
    public String getRegisterpage(){

        return "/site/register";
    }

    //访问登录页面
    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }


    //提交数据post
    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public String register(Model model,User user){
        Map<String,Object> map = userService.register(user);
        if(map==null|| map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    //http://localhost:8080/community/activation/101(用户id)/code(激活码)

    @RequestMapping(value = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int result = userService.activation(userId,code);
        Context context = new Context();
        if(result == ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作，该账号已经激活过了！");
            model.addAttribute("target","/index");

        } else if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功！");
            model.addAttribute("target","/login");
        } else {
            model.addAttribute("msg","激活失败，激活码不正确！");
            model.addAttribute("target","/index");
        }
        return  "/site/operate-result";
    }

//    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
//    //因为向浏览器传的是图片不是字符串什么的，所以不能用responsebody自动返回值，可以在方法体中自己用response接受
//    public void getKaptcha(HttpServletResponse response, HttpSession session){
//        //生成验证码
//        String text = kaptchaProducer.createText();
//        BufferedImage image = kaptchaProducer.createImage(text);
//        //将验证码存入session
//        session.setAttribute("kaptcha",text);
//
//        //将图片输出给浏览器
//        response.setContentType("image/png");
//        try {
//            OutputStream os = response.getOutputStream();
//            ImageIO.write(image,"png",os);
//        } catch (IOException e){
//            logger.error("响应验证码失败:"+ e.getMessage());
//        }
//    }

    //利用redis重构登录凭证保存，用redis替换session
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    //因为向浏览器传的是图片不是字符串什么的，所以不能用responsebody自动返回值，可以在方法体中自己用response接受
    public void getKaptcha(HttpServletResponse response){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        //让它很快失效60s
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        //将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);


        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e){
            logger.error("响应验证码失败:"+ e.getMessage());
        }
    }

    //可以路径是一样的，但是需要方法类型不同
    //如果参数不是普通参数，而是user等实体类，springmvc就会把参数装在model里，页面就可以通过model获取,即${}，model把参数返回给页面
    //model里的参数可以直接${名称}调用，但是参数里的需要加param前缀，即${param.参数}
    //普通参数要获取要么主动获取，要么加入到手动model
//    @RequestMapping(path = "/login",method = RequestMethod.POST)
//    //code验证码,ticket用cookie保存，用httpservlert
//    public String login(String username, String password, String code,boolean rememberme,
//                        Model model,HttpSession session,HttpServletResponse response){
//        String kaptcha = (String) session.getAttribute("kaptcha");
//        //检查验证码，业务层未包括这个功能，不区分大小写
//        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
//            model.addAttribute("codeMsg","验证码不正确！");
//            return "/site/login";
//        }
//        //检查账号密码
//        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
//        Map<String,Object> map = userService.login(username,password,expiredSeconds);
//        //如果map里包含ticket就是登陆成功,成功时生成登录凭证发给客户端
//        if(map.containsKey("ticket")){
//            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
//            cookie.setPath(contextPath);
//            cookie.setMaxAge(expiredSeconds);
//            response.addCookie(cookie);
//            return "redirect:/index";
//        }
//        else{
//            model.addAttribute("usernameMsg",map.get("usernameMsg"));
//            model.addAttribute("passwordMsg",map.get("passwordMsg"));
//            return "/site/login";
//        }
//    }

    //改写，将从session取值改成从redis取值
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    //code验证码,ticket用cookie保存，用httpservlert
    //从cookie取出生成kaptchakey用的随机owner值
    public String login(String username, String password, String code,boolean rememberme,
                        Model model,HttpSession session,HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String  kaptchaOwner){
        String kaptcha = null ;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(kaptchaKey);
        }


        //检查验证码，业务层未包括这个功能，不区分大小写
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/login";
        }
        //检查账号密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = userService.login(username,password,expiredSeconds);
        //如果map里包含ticket就是登陆成功,成功时生成登录凭证发给客户端
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }
        else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        //重定向默认是get请求
        return "redirect:/login";
    }



}
