package com.entingwu.ecommerceseckill.db.dao;

import com.entingwu.ecommerceseckill.db.po.SeckillOrder;

public interface SeckillOrderDao {

    public void insertSeckillOrder(SeckillOrder order);

    public SeckillOrder querySeckillOrder(String orderNo);

    public void updateSeckillOrder(SeckillOrder order);
}
