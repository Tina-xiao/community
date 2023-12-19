package com.nju.community.controller;


import com.nju.community.annotation.LoginRequired;
import com.nju.community.entity.User;
import com.nju.community.service.FollowService;
import com.nju.community.util.CommunityUtil;
import com.nju.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

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


}

