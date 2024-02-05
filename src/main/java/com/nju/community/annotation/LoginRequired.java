package com.nju.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Deprecated
//表示注解可以写在方法之上
@Target(ElementType.METHOD)
//程序运行时有效
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

    //登录后才能访问,用拦截器拦截带有该注解的方法，登录才可以访问被注解标注的web路径

}
