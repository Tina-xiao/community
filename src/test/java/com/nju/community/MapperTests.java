package com.nju.community;

import com.nju.community.dao.DiscussPostMapper;
import com.nju.community.dao.LoginTicketMapper;
import com.nju.community.entity.DiscussPost;
import com.nju.community.entity.LoginTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0,10);
        for(DiscussPost post: list)
            System.out.println(post);

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    public void testLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket("asdf");
        loginTicket.setUserId(101);
        loginTicket.setStatus(0);
        //1000ms * 60 *10
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
//        loginTicketMapper.insertLoginTicket(loginTicket);
//        System.out.println(loginTicketMapper.selectByTicket("asdf"));
        loginTicketMapper.updateStatus("asdf",1);
    }
}
