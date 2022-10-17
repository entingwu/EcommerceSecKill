package com.entingwu.ecommerceseckill.mq;

import com.alibaba.fastjson.JSON;
import com.entingwu.ecommerceseckill.db.dao.SeckillActivityDao;
import com.entingwu.ecommerceseckill.db.po.SeckillOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Transactional
@RocketMQMessageListener(topic = "pay_done", consumerGroup = "pay_done_group")
public class PayDoneConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Override
    public void onMessage(MessageExt messageExt) {
        // 1. Parse request message to create order
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received request to create order: " + message);
        SeckillOrder order = JSON.parseObject(message, SeckillOrder.class);
        // 2. Deduct inventory
        seckillActivityDao.deductStock(order.getSeckillActivityId());
    }
}
