package com.nju.community.controller;

import com.nju.community.entity.Comment;
import com.nju.community.entity.DiscussPost;
import com.nju.community.entity.Event;
import com.nju.community.event.EventProducer;
import com.nju.community.service.CommentService;
import com.nju.community.service.DiscussPostService;
import com.nju.community.util.CommunityConstant;
import com.nju.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;


    @RequestMapping(value = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        //discussPostId是评论的帖子的id，即当前在哪个帖子页面，但comment的entityId不一定就是discussPostId,因为有可能是评论的该帖子的评论
        //声明comment接收数据
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        //默认有效
        comment.setStatus(0);
        commentService.addComment(comment);

        //触发评论事件,评论之后发系统通知
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(comment.getUserId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                //不是所有的时间都需要帖子id，所以存data里，这个帖子id用于发布评论后返回帖子页面
                .setData("discussPostId",discussPostId);

                //如果评论回复的是人，那么entityUserId是空的。
                if(comment.getEntityType() == ENTITY_TYPE_POST){
                    DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
                    event.setEntityUserId(target.getUserId());
                } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
                    Comment target = commentService.findCommentById(comment.getEntityId());
                    event.setEntityUserId(target.getUserId());
                }
                //触发事件
                eventProducer.triggerEvent(event);

        return "redirect:/discuss/detail/" + discussPostId;
    }

}
