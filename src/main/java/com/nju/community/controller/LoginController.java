package com.nju.community.controller;


import com.nju.community.entity.User;
import com.nju.community.service.UserService;
import com.nju.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.context.Context;

import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

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

}
