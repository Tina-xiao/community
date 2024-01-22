package com.nju.community.entity;

import java.util.HashMap;
import java.util.Map;

//封装事件对象，用于kafka发送异步消息
public class Event {

    private String topic;
    //事件触发人
    private int userId;
    //被触发的实体类型
    private int entityType;
    //被触发的实体id
    private int entityId;
    //帖子作者
    private int entityUserId;
    //对于事件其他的可能的属性，设置一个map来存放，使event有扩展性
    private Map<String, Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getUserId() {
        return userId;
    }

    //改造get set方法,使之可以链式调用, .setTopic().setType()....
    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return  this;
    }


}
