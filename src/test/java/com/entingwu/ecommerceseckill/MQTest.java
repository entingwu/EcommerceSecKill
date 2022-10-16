package com.entingwu.ecommerceseckill;

import com.entingwu.ecommerceseckill.mq.RocketMQService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class MQTest {

    @Autowired
    RocketMQService service;

    @Test
    public void sendMQTest() throws Exception {
        // ConsumerListener listens to the same topic
        service.sendMessage("test-seckill", "Hello world! " + new Date().toString());
    }
}
