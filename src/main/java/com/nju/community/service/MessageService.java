package com.nju.community.service;


import com.nju.community.dao.MessageMapper;
import com.nju.community.entity.Message;
import com.nju.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;


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

    public int addMessage(Message message){
        //添加前需要过滤消息
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int updateStatus(List<Integer> ids, int status){
        return messageMapper.updateStatus(ids,status);
    }

    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    public int findNoticeCount(int userId, String topic){
        return messageMapper.selectNoticCount(userId, topic);
    }

    public int findNoticeUnreadCount(int userId, String topic){
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return  messageMapper.selectNotices(userId, topic, offset, limit);
    }

}
