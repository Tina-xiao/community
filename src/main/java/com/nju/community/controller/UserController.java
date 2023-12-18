package com.nju.community.controller;


import com.nju.community.annotation.LoginRequired;
import com.nju.community.entity.User;
import com.nju.community.service.LikeService;
import com.nju.community.service.UserService;
import com.nju.community.util.CommunityUtil;
import com.nju.community.util.HostHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    //获取当前用户
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    //上传头像
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        //后缀,获取文件名最后一个点的位置，向后截取就是后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("erro","文件格式不正确！");
            return "/site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        //确定文件存放路径
        File dest = new File(uploadPath+"/"+filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常",e);
        }

        //更新当前用户的头像的路径(Web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/"+filename;
        userService.updateHeader(user.getId(),headerUrl);

        //这里的/index是指web访问路径，而不是静态资源路径，但上面的return "/site/setting"是指静态资源路径
        return "redirect:/index";
    }


    @RequestMapping(path = "/header/{fileName}" , method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/" + suffix);
        try (
                //读取文件得到输入流,在try的括号里创建对象，编译时自动加上finnaly关闭掉
                FileInputStream fis = new FileInputStream(fileName);
                ) {
            //输出流由response创建，springmvc自动关闭，但是输入流fis是我们手动创建，需要手动关闭
            OutputStream os = response.getOutputStream();

            //缓冲区1024bytes
            byte[] buffer = new byte[1024];
            int b=0;
            //b是读取的字节总数，一般为1024，如果缓冲区没读满那就是<1024
            while( (b=fis.read(buffer)) != -1){
                //从缓冲区0~b读取
                os.write(buffer,0,b);
            }
            fis.close();
        } catch (IOException e) {
            logger.error("读取图片失败"+e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @LoginRequired
    @RequestMapping(path = "/updatePassword",method = RequestMethod.POST)
    public String modifyPassword(String originPassword ,String newPassword,Model model){
        User user = hostHolder.getUser();
        Map<String,Object> map = userService.updatePassword(user.getId(),originPassword,newPassword);
        if(map.isEmpty() || map==null){
            model.addAttribute("success","密码修改成功！即将跳转到登录页面，请重新登陆！");
            return "redirect:/login";
        }
        else {
            model.addAttribute("originPasswordMsg",map.get("originPasswordMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    //查看自己和别人的个人主页
    @RequestMapping(path = "/profile/{userId}" , method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        //用户
        model.addAttribute("user",user);
        //用户获赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        return "/site/profile";
    }


}
