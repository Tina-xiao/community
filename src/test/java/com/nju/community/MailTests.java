package com.nju.community;
import com.nju.community.util.MailClient;
import org.apache.naming.ContextAccessController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;


    @Autowired
    private TemplateEngine engine;

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","Tina");

        //模板引擎的作用时生成动态网页
        String content = engine.process("/mail/demo",context);
        System.out.println(content);
        mailClient.sendMail("1597872231@qq.com","html",content);
    }

    @Test
    public void sendMail(){
        mailClient.sendMail("1597872231@qq.com","xiaoxiao","hello,dear!");
    }

}
