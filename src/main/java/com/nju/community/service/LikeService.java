package com.nju.community.service;

import com.nju.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞
//    public void like(int userId, int entityType, int entityId) {
//        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        //同一个用户第一次是点赞第二次是取消
//        boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
//        if (isMember) {
//            redisTemplate.opsForSet().remove(key, userId);
//        } else {
//            redisTemplate.opsForSet().add(key, userId);
//        }
//
//    }

    //点赞，启用事务,点赞同时要给被赞的作者获得赞数+1
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //先查询再开启事务，因为redis的特性
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                operations.multi();
                //同一个用户第一次是点赞第二次是取消

                if(isMember){
                    operations.opsForSet().remove(entityLikeKey, userId);
                    //取消点赞的话，user对应的点赞数value-1
                    operations.opsForValue().decrement(userLikeKey);
                }
                else{
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });

    }

    //查询某实体点赞数量
    public long findEntityLikeCount(int entityType, int entityId){
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(key);
    }

    //查询某人对某实体点没点过赞
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        if( redisTemplate.opsForSet().isMember(key, userId))
            return 1;
        else{
            return 0;
        }
    }

    //查询某个用户获得的赞
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count==null? 0 :count.intValue();
    }


}