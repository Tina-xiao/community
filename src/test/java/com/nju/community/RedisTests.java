package com.nju.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;
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

    //统计二十万重复数据的独立总数,HyperLogLog是估算，有误差的
    @Test
    public void testHyperLogLog() {
        //HyperLogLog在大数据量级的情况下能够在很小的空间中进行元素去重统计
        //通常用于统计UV:是指从00:00-24:00内相同的客户端的单次或者多次访问标记为一次访问。
        String redisKey = "test:hyperloglog:01";
        for (int i = 1; i < 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }
        for (int i = 1; i < 100000; i++) {
            int r = (int)Math.random()*100000 + 1;
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }

        long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);

    }

    //将三组数据合并，再统计合并后的重复数据的独立总数
    @Test
    public void testHyperLogLog2(){
        String redisKey2 = "test:hyperloglog:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }
        String redisKey3 = "test:hyperloglog:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }
        String redisKey4 = "test:hyperloglog:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }
        String unionKey = "test:hyperloglog:union";
        redisTemplate.opsForHyperLogLog().union(unionKey,redisKey2,redisKey3,redisKey4);
        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }

    //统计一组数据布尔值
    @Test
    public void testBitMap() {
        //类似于String数据类型，每个位置上只有boolean类型，即只有01两种可能
        String redisKey = "test:bitmap:01";

        //记录,没存的index默认为false
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 3, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        //查询某一位结果
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,5));

        //统计不在对象里,需要通过redis连接获取size,统计true的个数
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });
        //true的个数
        System.out.println(obj);

    }


    //统计三组数据的布尔值，并做OR运算
    @Test
    public void testBitMapOperation() {


        //类似于String数据类型，每个位置上只有boolean类型，即只有01两种可能
        String redisKey2 = "test:bitmap:02";

        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
       // redisTemplate.opsForValue().setBit(redisKey2, 2, false);


        String redisKey3 = "test:bitmap:03";
        //记录,没存的index默认为false
       //redisTemplate.opsForValue().setBit(redisKey3, 2, false);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);

        String redisKey4 = "test:bitmap:04";
        //记录,没存的index默认为false
        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
       // redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.AND,
                        redisKey.getBytes(),redisKey2.getBytes(),redisKey3.getBytes(),redisKey4.getBytes());
                return connection.bitCount(redisKey.getBytes());
            }
        });
        //3
        System.out.println(obj);

//true
//true
//false
//false
//false
//false
//false
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));

    }




}
