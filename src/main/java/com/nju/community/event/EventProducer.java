package com.nju.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.nju.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    //spring自动装配
    @Autowired
    private KafkaTemplate kafkaTemplate;

    //处理事件即发送消息
    public void triggerEvent (Event event) {
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
