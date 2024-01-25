package com.nju.community;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTests {

    @Autowired
    private KafkaProducer producer ;

    //运行前要先运行kafka！如果kafka出现broker warn就把log目录删掉重启
    @Test
    public void testKafka() {
        producer.sendMessage("test","hello world");
        producer.sendMessage("test","hello world2ssss");
        producer.sendMessage("test1","lcx is good");

        try {
            Thread.sleep(1000*10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

//生产者发消息是主动的，什么时候想发就调用producer
//消费者接收消息是被动的，一有消息就接收
@Component
class KafkaProducer {

    //spring自动装配
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage (String topic, String content) {
        kafkaTemplate.send(topic, content);
    }


}

@Component
class KafkaConsumer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    //监听消息队列，一有消息就读，没有消息就阻塞，读出来的消息给下面修饰的方法处理
    @KafkaListener(topics = {"test","test1"})
    public void handleMessage(ConsumerRecord record) {
        System.out.println(record.value());
    }
}