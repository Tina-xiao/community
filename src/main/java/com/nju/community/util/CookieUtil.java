package com.nju.community.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {

    //封装request获取cookie,返回cookie中存的ticket，name就是cookie中的那个key
    public static String getValue(HttpServletRequest request, String name){
        if(request==null || name==null)
            throw new IllegalArgumentException("参数为空!");

        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for(Cookie cookie:cookies){
                if(cookie.getName().equals(name))
                    return cookie.getValue();
            }
        }
        return null;
    }
}
