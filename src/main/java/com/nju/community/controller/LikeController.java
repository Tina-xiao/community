package com.nju.community.controller;

import com.nju.community.entity.Event;
import com.nju.community.entity.User;
import com.nju.community.event.EventProducer;
import com.nju.community.service.LikeService;
import com.nju.community.util.CommunityConstant;
import com.nju.community.util.CommunityUtil;
import com.nju.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;


    @RequestMapping(path="/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int discussPostId) {
        User user = hostHolder.getUser();
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        //返回点赞数量
        Long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        //状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        //返回的结果
        Map<String,Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        //likeStatus为1表明当前是点完赞的状态，再点一下是取消点赞，为0
        map.put("likeStatus", likeStatus);

        //如果点赞触发点赞事件，取消点赞就不通知了
        if(likeStatus == 1){

            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setUserId(user.getId())
                    //用于提供返回的帖子链接
                    .setData("discussPostId",discussPostId);

            eventProducer.triggerEvent(event);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }

}
