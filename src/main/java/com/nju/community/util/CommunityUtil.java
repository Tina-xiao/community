package com.nju.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class CommunityUtil {
    //生成随机字符串
    public static String generateUUID(){
        //有字母和横线，不想要横线，替换成空
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5加密
    //hello ->abc123def456,每次加密的结果都是一样的，于是改进成将要加密的字符串加上一段随机字符salt，然后加密
    public static String md5(String key){
        if(StringUtils.isBlank(key))//import org.apache.commons.lang3.StringUtils; null,空串，只有空格都判定为空
            return null;
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
