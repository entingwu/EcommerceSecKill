package com.entingwu.ecommerceseckill.mq;

import com.alibaba.fastjson.JSON;
import com.entingwu.ecommerceseckill.db.dao.SeckillActivityDao;
import com.entingwu.ecommerceseckill.db.dao.SeckillOrderDao;
import com.entingwu.ecommerceseckill.db.po.SeckillOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RocketMQMessageListener(topic = "seckill_order", consumerGroup = "seckill_order_group")
public class OrderConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    private SeckillOrderDao seckillOrderDao;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        // 1. Parse the request of order creation
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Receive the request to create an order" + message);
        SeckillOrder order = JSON.parseObject(message, SeckillOrder.class);
        order.setCreateTime(new Date());

        // 2. Stock deduction
        boolean lockStockResult = seckillActivityDao.lockStock(order.getSeckillActivityId());
        if (lockStockResult) {
            // lock successfully:
            // 1: already created, wait for payment.
            order.setOrderStatus(1);
        } else {
            // 0: invalid order, no available stock
            order.setOrderStatus(0);
        }

        // 3. Insert Order
        seckillOrderDao.insertSeckillOrder(order);
    }
}
