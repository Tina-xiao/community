package com.nju.community.dao;

import com.nju.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;


import java.util.List;

@Mapper
public interface CommentMapper {

    //返回每页的评论数，offset是指起始索引，从0开始，limit是从起始索引开始取几条记录，entitytype是被评论实体的类型
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);
    //一共有多少条数据，返回总评论数计算页数
    int selectCountByEntity(int entityType, int entityId);

}
