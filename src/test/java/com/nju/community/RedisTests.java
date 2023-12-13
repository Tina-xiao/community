package com.nju.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {
    @Autowired
    private RedisTemplate redisTemplate;

    //访问String
    @Test
    public void testStrings(){
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey,1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));

    }

    //访问Hash
    @Test
    public void testHashes(){
        //实际上是"test_hash",只是redis标准要求不同单词之间用:分隔
        String redisKey = "test:hash";
        redisTemplate.opsForHash().put(redisKey,"name","zhangsan");
        redisTemplate.opsForHash().put(redisKey,"age",18);

        System.out.println(redisTemplate.opsForHash().get(redisKey,"name"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"age"));

    }
    //访问List
    @Test
    public void testLists(){
        String redisKey = "test:list";
        redisTemplate.opsForList().leftPush(redisKey,110);
        redisTemplate.opsForList().leftPush(redisKey,112);
        redisTemplate.opsForList().rightPush(redisKey,114);
        redisTemplate.opsForList().rightPush(redisKey,114);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,3));

        redisTemplate.opsForList().rightPop(redisKey);
        redisTemplate.opsForList().leftPop(redisKey);
        System.out.println(redisTemplate.opsForList().range(redisKey,0,1));
    }

    //访问Set,集合不允许有重复
    @Test
    public void testSets(){
        String redisKey = "test:set";
        redisTemplate.opsForSet().add(redisKey,101,102,103,"104","小明");
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
        //随机弹出一个值
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        //查看所有元素
        System.out.println(redisTemplate.opsForSet().members(redisKey));

    }

    //有序集合,即每个值都带有一个分数，根据这个分数排序。
    @Test
    public void testSortedSets(){
        String redisKey = "test:sortedSet";

        redisTemplate.opsForZSet().add(redisKey,"小明",60);
        redisTemplate.opsForZSet().add(redisKey,"小红",65);
        redisTemplate.opsForZSet().add(redisKey,"小绿",40);
        redisTemplate.opsForZSet().add(redisKey,"小李",90);

        System.out.println(redisTemplate.opsForZSet().size(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"小红"));
        System.out.println(redisTemplate.opsForZSet().rank(redisKey,"小红"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"小红"));
        System.out.println(redisTemplate.opsForZSet().range(redisKey,0,3));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,3));
    }

    @Test
    public void testKeys(){
        redisTemplate.delete("test:set");

        System.out.println(redisTemplate.hasKey("test:set"));
        //10s后删除
        redisTemplate.expire("test:hash",10, TimeUnit.SECONDS);

    }

    //简化方案，绑定key,多次访问同一key
    @Test
    public void testBoundOperations(){
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        System.out.println(operations.get());
        operations.increment();
        System.out.println(operations.get());
        operations.set(20);
        System.out.println(operations.get());
    }

    //编程事务
    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                //启用事务
                operations.multi();

                operations.opsForSet().add(redisKey,"张三","李四",1232);
                operations.opsForSet().add(redisKey,56,45);
                //在redis管理事务时不要做查询，无效，这里输出[],因为redis是统一提交数据，这里不会提交查询
                System.out.println(operations.opsForSet().members(redisKey));

                //提交事务
                return operations.exec();
            }
        });
       // [3, 2, [56, 张三, 1232, 李四, 45]] ,3,2是每次影响的行数
        System.out.println(obj);
    }

}
