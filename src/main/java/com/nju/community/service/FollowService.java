package com.nju.community.service;

import com.nju.community.entity.User;
import com.nju.community.util.CommunityConstant;
import com.nju.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    //关注,userId关注了entity
    public void follow(int userId, int entityId, int entityType){
        //两次提交所以采用事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }

    //取消关注,userId关注了entity
    public void unfollow(int userId, int entityId, int entityType){
        //两次提交所以采用事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }

    //查询某用户关注的实体的数量
    public long findFolloweeCount(int userId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询某实体的粉丝数量
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }
    //查询某个用户关注的人，支持分页,按照时间排序

    //offset起始索引，limit一页最多有多少索引
    public List<Map<String,Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        //倒序，最新的时间在最前面
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset+limit-1);
        if(targetIds == null) {
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for(Integer id : targetIds) {
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user",user);
            //查关注时间
            Double score = redisTemplate.opsForZSet().score(followeeKey, id);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    //查询某个用户的粉丝，支持分页
    public List<Map<String,Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER , userId);
        Set<Integer> followerIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset+limit-1);
        if(followerIds == null)
            return null;
        List<Map<String,Object>> res = new ArrayList<>();
        for(Integer id: followerIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, id);
            map.put("followTime",new Date(score.longValue()));
            res.add(map);
        }
        return res;
    }

}
