package com.entingwu.ecommerceseckill.mq;

import com.alibaba.fastjson.JSON;
import com.entingwu.ecommerceseckill.db.dao.SeckillActivityDao;
import com.entingwu.ecommerceseckill.db.dao.SeckillOrderDao;
import com.entingwu.ecommerceseckill.db.po.SeckillOrder;
import com.entingwu.ecommerceseckill.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_check", consumerGroup = "pay_check_group")
public class PayStatusCheckListener implements RocketMQListener<MessageExt> {

    @Autowired
    private SeckillOrderDao seckillOrderDao;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RedisService redisService;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Received the order payment status verification message: " + message);
        SeckillOrder order = JSON.parseObject(message, SeckillOrder.class);
        // 1. Query order
        SeckillOrder orderInfo = seckillOrderDao.querySeckillOrder(order.getOrderNo());
        // 2. Check if the order payment has been completed
        if (orderInfo.getOrderStatus() != 2) {
            // 3. Payment is not completed, close order.
            log.info("Close order as the payment is not completed, orderNo: " + orderInfo.getOrderNo());
            orderInfo.setOrderStatus(99);
            seckillOrderDao.updateSeckillOrder(orderInfo);
            // 4. Restore inventory in db
            seckillActivityDao.revertStock(order.getSeckillActivityId());
            // 5. Restore redis inventory
            redisService.revertStock("stock:" + order.getSeckillActivityId());
            // 6. Remove user from limit purchased list
            redisService.removeLimitMember(order.getSeckillActivityId(), order.getUserId());
        }
    }
}
