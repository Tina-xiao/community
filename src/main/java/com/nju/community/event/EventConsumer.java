package com.nju.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.nju.community.entity.Event;
import com.nju.community.entity.Message;
import com.nju.community.service.MessageService;
import com.nju.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {


    @Autowired
    private MessageService messageService;



    //记录日志，防止出现问题
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    //监听消息队列，一有消息就读，没有消息就阻塞，读出来的消息给下面修饰的方法处理
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleEvents(ConsumerRecord record) {
        if(record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null){
            logger.error("消息格式错误！");
            return;
        }
        //我们定义系统给发的通知的发送人id永远为，那么conversationID就没有必要存为111_112的形式，改存为topic

        //发送站内通知，构造message
        //最后站内显示的消息格式，例: 用户 lili 评论/点赞/关注 了你的帖子,点击查看！(url)
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        //存的是主题
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        Map<String,Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        if(!event.getData().isEmpty()){
            for(Map.Entry<String, Object> entry: event.getData().entrySet()){
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        System.out.println("111111111111111111111111");
        messageService.addMessage(message);

    }
}
