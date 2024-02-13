package com.nju.community.util;

//用来生成redis的key
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    //A关注B，A是B的follower，B是followee
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    //unique visitor独立访客
    private static final String PREFIX_UV = "uv";
    //daily active user日活跃用户
    private static final String PREFIX_DAU = "dau";
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

    //某个人用户关注的实体
    //followee:userId:entityType -> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT +userId + SPLIT + entityType ;
    }

    //某个实体拥有的粉丝
    //follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT +entityType + SPLIT + entityId;
    }

    //登录验证码,重构
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner ;
    }

    //登录凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket ;
    }
    //用户
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT +  userId;
    }

    //单日uv
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }
    //区间UV
    public static String getUVKey(String startDate, String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }
    //单日DAU
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }
    //区间dau
    public static String getDAUKey(String startDate, String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }
}
