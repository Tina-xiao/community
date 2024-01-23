package com.nju.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nju.community.entity.Comment;
import com.nju.community.service.CommentService;
import com.nju.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(value = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        //discussPostId是评论的帖子的id，即当前在哪个帖子页面，但comment的entityId不一定就是discussPostId,因为有可能是评论的该帖子的评论
        //声明comment接收数据
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        //默认有效
        comment.setStatus(0);
        commentService.addComment(comment);
        return "redirect:/discuss/detail/" + discussPostId;
    }

}
