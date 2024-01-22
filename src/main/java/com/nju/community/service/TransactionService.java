package com.nju.community.service;

import com.nju.community.dao.DiscussPostMapper;
import com.nju.community.dao.UserMapper;
import com.nju.community.entity.DiscussPost;
import com.nju.community.entity.User;
import com.nju.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;

@Service
public class TransactionService {
    //mvc自动装配到容器,利用它提交sql会保证事务四个特性
    @Autowired
    private TransactionTemplate transactionTemplate;

   //模仿发帖的事务
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    
    //声明式事务
    //设置隔离级别(Isolation)和传播机制（如果业务方法A调业务方法B，且AB都有事务机制（有transactional注解），A是外部事务）
    //REQUIRED:支持当前事务（外部事物），如果不存在则创建新事务，而由于AB两个方法使用了同一个事务，其中任何一个回滚都回同时回滚。
    //REQUIRES_NEW:创建一个新事务，并且暂停当前事务（外部事务）
    //NESTED:如果当前存在事务（外部事务），则嵌套在该事物中执行（有独立的提交和回滚），如果不存在就和REQUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save(){

        // 新增用户
        User user = new User();
        user.setUsername("lcx");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);


        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("TransactionTest");
        post.setContent("新人报道！");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        //人为制造错误，abc无法转成整型,因此函数的所有事务都回滚不会提交
        Integer.valueOf("abc");

        return "ok";
    }
    
    //编程式事务
    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {

                // 新增用户
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                //新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("TransactionTest2");
                post.setContent("新人报道！");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                //人为制造错误，abc无法转成整型,因此函数的所有事务都回滚不会提交
                Integer.valueOf("abc");

                return "ok";

            }
        });
    }

}
