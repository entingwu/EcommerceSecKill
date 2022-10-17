package com.entingwu.ecommerceseckill.service;

import com.alibaba.fastjson.JSON;
import com.entingwu.ecommerceseckill.db.dao.SeckillActivityDao;
import com.entingwu.ecommerceseckill.db.dao.SeckillOrderDao;
import com.entingwu.ecommerceseckill.db.po.SeckillActivity;
import com.entingwu.ecommerceseckill.db.po.SeckillOrder;
import com.entingwu.ecommerceseckill.mq.RocketMQService;
import com.entingwu.ecommerceseckill.util.RedisService;
import com.entingwu.ecommerceseckill.util.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class SeckillActivityService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    @Autowired
    private SeckillOrderDao seckillOrderDao;

    @Autowired
    private RocketMQService rocketMQService;

    private SnowFlake snowFlake = new SnowFlake(1, 1);

    /**
     * Check it the commodity is in stock
     * @param activityId 商品ID
     * @return
     */
    public boolean seckillStockValiator(long activityId) {
        String key = "stock:" + activityId;
        return redisService.stockDeductValidator(key);
    }

    public SeckillOrder createOrder(long seckillActivityId, long userId) throws Exception {
        SeckillActivity activity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        SeckillOrder order = new SeckillOrder();
        // 1. generate order using snowflake algorithm
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(activity.getId());
        order.setUserId(userId);
        order.setOrderAmount(activity.getSeckillPrice().longValue());

        // 2. send message of order creation
        // OrderConsumer
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));

        // 3. send order payment status message for verification
        // 开源RocketMQ支持延迟消息，但是不支持秒级精度。默认支持18个level的延迟消息，这是通过broker端的messageDelayLevel配置项确定的，如下：
        // messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
        rocketMQService.sendDelayMessage("pay_check", JSON.toJSONString(order), 3);
        return order;
    }

    /**
     * Order payment process
     *
     * @param orderNo
     */
    public void payOrderProcess(String orderNo) throws Exception {
        log.info("Order payment is completed, orderNo: " + orderNo);
        SeckillOrder order = seckillOrderDao.querySeckillOrder(orderNo);
        /**
         * 1. Check if order exists
         *    Check if order status is unpaid
         */
        if (order == null) {
            log.error("The order of the corresponding orderNo does not exist: " + orderNo);
        } else if (order.getOrderStatus() != 1) {
            log.error("Invalid order status: " + orderNo);
            return;
        }

        /**
         * 2. Order payment is completed
         */
        order.setPayTime(new Date());
        // 0 No available stock, invalid order
        // 1 created and waited for payment
        // 2 payment is completed
        order.setOrderStatus(2);
        seckillOrderDao.updateSeckillOrder(order);

        /**
         * 3. Send order payment success message
         */
        rocketMQService.sendMessage("pay_done", JSON.toJSONString(order));
    }
}
