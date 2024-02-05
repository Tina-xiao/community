package com.nju.community.service;

import com.nju.community.dao.LoginTicketMapper;
import com.nju.community.dao.UserMapper;
import com.nju.community.entity.LoginTicket;
import com.nju.community.entity.User;
import com.nju.community.util.CommunityConstant;
import com.nju.community.util.CommunityUtil;
import com.nju.community.util.MailClient;
import com.nju.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

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
//        return userMapper.selectById(id);
        //先从cache里查
        User user = getCache(id);
        if(user==null)
            user = init(id);
        return user;
    }

    public User findUserByName(String name) {
        return userMapper.selectByName(name);
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
            //一旦user信息变化，cache就清除，下次重新存
            clearCache(userId);
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
            System.out.println("获取的密码加密后"+password);
            System.out.println("仓库密码"+user.getPassword());
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
        //loginTicketMapper.insertLoginTicket(loginTicket);
        //使用redis存取登录凭证，因为登录凭证调取频繁
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        //把对象序列化成json字符串当作value存到redis
        redisTemplate.opsForValue().set(ticketKey, loginTicket);


        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        //登出后ticket失效
//        loginTicketMapper.updateStatus(ticket,1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);//表示删除
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

    }

    //通过凭证找到对应的登录记录
    public LoginTicket findLoginTicket(String ticket){
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    public int updateHeader(int userId, String headerUrl){
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }

    public Map<String,Object> updatePassword(int userId,String originPassword,String newPassword){
        Map<String,Object> map = new HashMap<>();
        User user = userMapper.selectById(userId);
        if(StringUtils.isBlank(originPassword) ){
            map.put("originPasswordMsg","密码不可为空");
            return map;
        }

        if(!CommunityUtil.md5(originPassword+user.getSalt()).equals(user.getPassword())){
            map.put("originPasswordMsg","初始密码不正确，请重新输入！");
            return map;
        }

        if(StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","密码不可为空");
            return map;
        }

        newPassword = CommunityUtil.md5(newPassword+user.getSalt());
        userMapper.updatePassword(userId,newPassword);
        clearCache(userId);
        return map;
    }
    //用户信息存到redis

    //1.优先从缓存取值
    private User getCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User)redisTemplate.opsForValue().get(userKey);
    }
    //2.取不到时初始化缓存数据
    private User init(int userId){
        //如果当前用户信息在redis取不到，就去sql查然后存到redis
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        // 给一个过期时间,因为是缓存
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }
    //3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        //直接把key以及存储的value删掉
        redisTemplate.delete(userKey);
    }

    //获得用户权限,返回用户权限list
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }


    public Collection<? extends GrantedAuthority> getAuthorities1(int userId) {//查询用户权限的方法，希望获得userId用户的权限
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();//什么时候获得用户权限，并且把用户权限的结论tocken存到context里，之前也做过显示用户登录信息的功能，登录成功以后会生成一个ticket，存到用户里，用户每次访问服务器，服务器会验证此ticket，看此凭证对不对，有没有过期
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }


}
