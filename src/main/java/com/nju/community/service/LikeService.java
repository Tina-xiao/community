package com.nju.community.service;

import com.nju.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞
    public void like(int userId, int entityType, int entityId) {
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //同一个用户第一次是点赞第二次是取消
        boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
        if (isMember) {
            redisTemplate.opsForSet().remove(key, userId);
        } else {
            redisTemplate.opsForSet().add(key, userId);
        }

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


}