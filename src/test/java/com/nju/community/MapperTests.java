package com.nju.community;

import com.nju.community.dao.DiscussPostMapper;
import com.nju.community.entity.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
<<<<<<< HEAD
import org.thymeleaf.TemplateEngine;
=======
>>>>>>> eaec2376b2b861e7669f9a352c6b274ddb9c6d4f

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

<<<<<<< HEAD

=======
>>>>>>> eaec2376b2b861e7669f9a352c6b274ddb9c6d4f
    @Test
    public void testSelectPosts(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0,10);
        for(DiscussPost post: list)
            System.out.println(post);

        int rows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

}
