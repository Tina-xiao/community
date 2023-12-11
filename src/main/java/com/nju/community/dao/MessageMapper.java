package com.nju.community.dao;


import com.nju.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId) ;
    int selectConversationCount1(int userId) ;


    //查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);


    //未读私信数量,一个是这个用户所有的未读私信数量，一个是当前会话的未读私信数量，conversationId动态拼上去，有就用没有就不用，这样可以实现两个功能
    int selectLetterUnreadCount(int userId, String conversationId);

    //新增一个消息
    int insertMessage(Message message);

    //更改多条消息状态
    int updateStatus(List<Integer> ids, int status);


}
