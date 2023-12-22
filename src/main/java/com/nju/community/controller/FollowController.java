package com.nju.community.controller;


import com.nju.community.annotation.LoginRequired;
import com.nju.community.entity.Page;
import com.nju.community.entity.User;
import com.nju.community.service.FollowService;
import com.nju.community.service.UserService;
import com.nju.community.util.CommunityConstant;
import com.nju.community.util.CommunityUtil;
import com.nju.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant{

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    //关注
    @LoginRequired
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityId, int entityType) {
        User user = hostHolder.getUser();
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        followService.follow(user.getId(),entityId,entityType);
        return CommunityUtil.getJSONString(0, "已关注");
    }

    //取消关注
    @LoginRequired
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityId, int entityType) {
        User user = hostHolder.getUser();
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        followService.unfollow(user.getId(),entityId,entityType);
        return CommunityUtil.getJSONString(0, "已取消关注");
    }

    //查看某用户的关注列表
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        //这是当前查看的主页的用户，主页的拥有者
        User user = userService.findUserById(userId);

        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        //传入当前主页显示的用户
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int)followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        List<Map<String, Object>> list = followService.findFollowees(userId, page.getOffset(), page.getLimit()) ;
        if(list!= null){
            //判断当前登录用户是否关注了这些用户
            for(Map<String,Object> map : list){
                User u = (User) map.get("user");
                boolean isFollow = hasFollowed(u.getId());
                map.put("hasFollowed",isFollow);
            }
        }
        model.addAttribute("users",list);
        return "/site/followee";
    }

    //判断当前登录用户是否关注了userId用户
    private boolean hasFollowed(int userId){
        //这是当前登录的用户
        User host = hostHolder.getUser();
        if(host==null)
            return false;
        boolean isFollow = followService.hasFollowed(host.getId(), ENTITY_TYPE_USER ,userId);
        return  isFollow;
    }

    //查看某用户的粉丝
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        //这是当前查看的主页的用户，主页的拥有者
        User user = userService.findUserById(userId);

        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int)followService.findFollowerCount(userId, ENTITY_TYPE_USER));
        List<Map<String, Object>> list = followService.findFollowers(userId, page.getOffset(), page.getLimit()) ;
        if(list!= null){
            //判断当前登录用户是否关注了这些用户
            for(Map<String,Object> map : list){
                User u = (User) map.get("user");
                boolean isFollow = hasFollowed(u.getId());
                map.put("hasFollowed",isFollow);
            }
        }
        model.addAttribute("users",list);
        return "/site/follower";
    }

}

