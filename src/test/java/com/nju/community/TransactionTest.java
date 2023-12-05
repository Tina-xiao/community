package com.nju.community;


import com.nju.community.service.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TransactionTest {

    @Autowired
    private TransactionService transactionService;

    @Test
    public  void save1(){
        Object obj = transactionService.save();
        System.out.println(obj);
    }

    @Test
    public  void save2(){
        Object obj = transactionService.save2();
        System.out.println(obj);
    }
}
