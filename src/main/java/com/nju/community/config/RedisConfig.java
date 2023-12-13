package com.nju.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    //RedisConnectionFactory已经被容器自动装配了在bean里
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        //指定序列化方式即数据转换方式，即pojo对象转换成什么类型存到redis数据库
        //设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //设置hash的key序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value序列化方式
        template.setHashValueSerializer(RedisSerializer.json());
        //设置完之后触发生效
        template.afterPropertiesSet();
        return template;
    }



 }
