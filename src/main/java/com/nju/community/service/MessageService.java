package com.nju.community.service;


import com.nju.community.dao.MessageMapper;
import com.nju.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;


    //查询当前用户的会话列表，针对每个会话只返回一条最新的私信
    public List<Message> findConversations(int userId, int offset, int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    };

    //查询当前用户的会话数量
    public int findConversationCount(int userId){
        return  messageMapper.selectConversationCount(userId);
    };



    //查询某个会话所包含的私信列表
    public List<Message> findLetters(String conversationId, int offset, int limit){
        return  messageMapper.selectLetters(conversationId,offset,limit);
    };

    //查询某个会话所包含的私信数量
    public int findLetterCount(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    };


    //未读私信数量,一个是这个用户所有的未读私信数量，一个是当前会话的未读私信数量，conversationId动态拼上去，有就用没有就不用，这样可以实现两个功能
    public int findLetterUnreadCount(int userId, String conversationId){
        return messageMapper.selectLetterUnreadCount(userId,conversationId);
    };
}
