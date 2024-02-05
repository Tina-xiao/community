package com.nju.community.util;

import com.alibaba.fastjson2.JSONObject;
import com.nju.community.entity.LoginTicket;
import com.nju.community.entity.User;
import com.nju.community.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.Map;
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

    //用于异步的时候传递消息给页面，code是错误编码，msg提示信息，map是要返回给浏览器的数据
    //code编码比如404，msg提示信息，map封装业务数据
    public static String getJSONString(int code, String msg, Map<String,Object> map)  {
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if(map != null){
            for(String key:map.keySet()){
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    //重载，因为有可能没有msg或者没有map
    public  static String getJSONString(int code, String msg){
        return getJSONString(code,msg,null);
    }
    public  static String getJSONString(int code){
        return getJSONString(code,null, null);
    }


}
