package com.nju.community.controller;

import com.nju.community.entity.DiscussPost;
import com.nju.community.entity.Page;
import com.nju.community.service.ElasticsearchService;
import com.nju.community.service.LikeService;
import com.nju.community.service.UserService;
import com.nju.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    //搜索到之后要展示帖子作者和帖子点赞数
    @Autowired
    private UserService userService;

    @Autowired
    public LikeService likeService;

    //get请求，keyword不能用请求体传，即/search/{keyword},不被允许
    //search?keyword= ,这样可以
    @RequestMapping(path = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        //搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
        elasticsearchService.searchDiscussPost(keyword, page.getCurrent()-1, page.getLimit());
        //聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if(searchResult != null) {
            for(var post: searchResult) {
                Map<String, Object> map = new HashMap<>();
                //帖子
                map.put("post",post);
                //帖子作者
                map.put("user",userService.findUserById(post.getUserId()));
                //点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword",keyword);

        //分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult==null ? 0 : (int) searchResult.getTotalElements());

        return "/site/search";
    }


}
