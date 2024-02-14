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

//单元测试
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    //对整个类做测试，就是跑一遍该test类内所有的@Test方法
    //也可以单独跑一个方法
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
        //删除测试数据
        discussPostService.updateStatus(data.getId(),2);

    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testFindById() {
        //测试findById功能
        DiscussPost discussPost = discussPostService.findDiscussPostById(data.getId());
        //判断是否非空
        Assert.assertNotNull(discussPost);
        //判断查到的数据和数据库中存的是否一致
        Assert.assertEquals(data.getTitle(), discussPost.getTitle());
        Assert.assertEquals(data.getContent(),discussPost.getContent());
    }

}
