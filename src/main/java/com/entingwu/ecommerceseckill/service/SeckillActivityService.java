package com.entingwu.ecommerceseckill.service;

import com.alibaba.fastjson.JSON;
import com.entingwu.ecommerceseckill.db.dao.SeckillActivityDao;
import com.entingwu.ecommerceseckill.db.po.SeckillActivity;
import com.entingwu.ecommerceseckill.db.po.SeckillOrder;
import com.entingwu.ecommerceseckill.mq.RocketMQService;
import com.entingwu.ecommerceseckill.util.RedisService;
import com.entingwu.ecommerceseckill.util.SnowFlake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillActivityService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SeckillActivityDao seckillActivityDao;

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
        // generate order using snowflake algorithm
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(activity.getId());
        order.setUserId(userId);
        order.setOrderAmount(activity.getSeckillPrice().longValue());

        // send message of order creation
        // OrderConsumer
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));
        return order;
    }
}
