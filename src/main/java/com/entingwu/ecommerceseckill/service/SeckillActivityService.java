package com.entingwu.ecommerceseckill.service;

import com.entingwu.ecommerceseckill.util.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillActivityService {

    @Autowired
    private RedisService redisService;

    /**
     * Check it the commodity is in stock
     * @param activityId 商品ID
     * @return
     */
    public boolean seckillStockValiator(long activityId) {
        String key = "stock:" + activityId;
        return redisService.stockDeductValidator(key);
    }
}
