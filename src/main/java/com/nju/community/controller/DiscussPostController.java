package com.nju.community.controller;

import com.nju.community.entity.Comment;
import com.nju.community.entity.DiscussPost;
import com.nju.community.entity.Page;
import com.nju.community.entity.User;
import com.nju.community.service.CommentService;
import com.nju.community.service.DiscussPostService;
import com.nju.community.service.LikeService;
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

import java.util.*;


@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private CommentService commentService;
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    //返回字符串不是网页-
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦!");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 报错的情况,将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    //查看某条帖子，以及该帖子的评论
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //点赞
        Long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        //点赞状态
        //要注意用户不登录也可以查看帖子详情页面，所以这里hostHolder.getUser()可能取null
        int likeStatus = hostHolder.getUser() == null? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        //某条帖子对应的评论总数，用于计算分页页数
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给评论的回复
        //得到当前帖子的所有评论
        List<Comment> commentList = commentService.findCommentByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        //ViewObject，用于封装页面显示的对象，评论对应的user
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList!=null){
            for(Comment comment: commentList){
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment",comment);
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                //点赞
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                //点赞状态
                //要注意用户不登录也可以查看帖子详情页面，所以这里hostHolder.getUser()可能取null
                likeStatus = hostHolder.getUser() == null? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);


                //回复列表，评论的评论,回复就不分页了，所以搞个最大值
                List<Comment> replyList = commentService.findCommentByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(),0,Integer.MAX_VALUE);
                //回复VO列表
                List<Map<String,Object> > replyVoList = new ArrayList<>();
                if(replyList!=null){
                    for(Comment reply: replyList){
                        Map<String , Object> replyVo = new HashMap<>();
                        replyVo.put("reply",reply);
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target = reply.getTargetId()==0 ? null:userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);

                        //点赞
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        //点赞状态
                        //要注意用户不登录也可以查看帖子详情页面，所以这里hostHolder.getUser()可能取null
                        likeStatus = hostHolder.getUser() == null? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);
                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);

                commentVoList.add(commentVo);
        }
        }

        model.addAttribute("comments",commentVoList);
        System.out.println(model.getAttribute("comments"));
        return "/site/discuss-detail";
    }

}
