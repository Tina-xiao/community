package com.nju.community;

import com.nju.community.entity.DiscussPost;
import com.nju.community.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    @Autowired
    private DiscussPostService discussPostService;

    //测试数据
    private DiscussPost data;

    @BeforeClass
    public static void beforeClass() {
        //只调一次
        System.out.println("BeforeClass");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("AfterClass");
    }

    @Before
    public void before() {
        //每次调测试方法前都要执行,无需静态
        System.out.println("before");

        data = new DiscussPost();
        data.setContent("test content");
        data.setTitle("test title");
        data.setUserId(111);
        data.setCreateTime(new Date());
        discussPostService.addDiscussPost(data);
    }

    @After
    public void after() {
        System.out.println("after");


    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

}
