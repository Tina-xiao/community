package com.nju.community.util;

//用来生成redis的key
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    //生成
    // 某个实体的赞的key
    //like:entity:entityType:entityId -> set(userId) 这样既能知道一共获得了几个赞也能知道谁给我点了赞，如果只用String存储没有这个效果
    public static String getEntityLikeKey(int entityType, int entityId){

        return PREFIX_ENTITY_LIKE + SPLIT +entityType + SPLIT +entityId;
    }

    //某个用户的赞
    //like:user:userId
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT +userId;
    }


}
