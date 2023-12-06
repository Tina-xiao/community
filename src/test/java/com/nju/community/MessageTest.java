package com.nju.community;


import com.nju.community.dao.MessageMapper;
import com.nju.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MessageTest {

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testMessage(){
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for(Message m:list){
            System.out.println(m);
        }
        System.out.println(messageMapper.selectConversationCount(111));
        System.out.println(messageMapper.selectConversationCount1(111));
        list = messageMapper.selectLetters("111_112",0,20);
        for(Message m: list){
            System.out.println(m);
        }
        System.out.println(messageMapper.selectLetterCount("111_112"));
        System.out.println(messageMapper.selectLetterUnreadCount(131, "111_131"));
        System.out.println(messageMapper.selectLetterUnreadCount(131,null));
    }

}
