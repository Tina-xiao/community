package com.nju.community.service;

import com.nju.community.dao.LoginTicketMapper;
import com.nju.community.dao.UserMapper;
import com.nju.community.entity.LoginTicket;
import com.nju.community.entity.User;
import com.nju.community.util.CommunityConstant;
import com.nju.community.util.CommunityUtil;
import com.nju.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;
    //领域
    @Value("${server.servlet.context-path}")
    private String contextPath;
    //    应用路径
    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        if(user==null)
            throw new IllegalArgumentException("参数不能为空");
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空!");
            return map;
        }
        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u !=null) {
            map.put("usernameMsg","账号已存在！");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if(u!=null) {
            map.put("emailMsg","该邮箱已被注册！");
            return map;
        }
        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
//        System.out.println("用户设置的密码"+user.getPassword());
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
//        System.out.println("加密后的密码"+user.getPassword());
        user.setStatus(0);//未激活
        user.setType(0);//普通用户
        user.setActivationCode(CommunityUtil.generateUUID());//激活码
        //头像路径
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);//这里插入之后mybatis才会给用户自动生成id，之前用户没有id

        //激活邮件
        Context context =new Context();
        context.setVariable("email",user.getEmail());
        //http://localhost:8080/community/activation/101(用户id)/code(激活码)
//        String url = domain+ contextPath+"/activation/"+ user.getId()+'/'+user.getActivationCode();
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        //返回页面字符串


        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return map;
    }

    //激活码逻辑处理
    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1)
            return  ACTIVATION_REPEAT;
        else if(user.getActivationCode().equals(code))
        {
//            user.setStatus(1);
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }
        else
            return ACTIVATION_FAILURE;

    }

    //过期时间expiredSeconds
    public Map<String,Object> login(String username, String password, int expiredSeconds){
        Map<String,Object>  map = new HashMap<>();

        //判断控制
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","消息不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return  map;
        }
        //验证账号
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        //验证状态
        if(user.getStatus()==0){
            map.put("usernameMsg","账号未激活");
            return map;
        }


        // 验证密码
        password = CommunityUtil.md5(password+user.getSalt());
        if (!user.getPassword().equals(password)) {
//            System.out.println("获取的密码加密后"+password);
//            System.out.println("仓库密码"+user.getPassword());
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        //随机生成一个凭证
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        //过期时间是当前时间往后推移expiredSecondss
        loginTicket.setExpired(new Date(System.currentTimeMillis()+ expiredSeconds *1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
         map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        //登出后ticket失效
        loginTicketMapper.updateStatus(ticket,1);

    }

}
