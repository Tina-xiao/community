package com.nju.community.service;


import com.nju.community.dao.DiscussPostMapper;
import com.nju.community.entity.DiscussPost;
import com.nju.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userid,int offset,int limit){
        return discussPostMapper.selectDiscussPosts(userid,offset,limit);
    }
    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int addDiscussPost(DiscussPost discussPost){
        if(discussPost==null){
            throw new IllegalArgumentException("参数不能为空!");
        }

        //转义html标记,如果title中有<script>abc</script> 等这种样式标记，添加到页面会损伤页面
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过滤敏感词
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public int updateCommentCount(int id, int count) {
        return discussPostMapper.updateCommentCount(id, count);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

}
