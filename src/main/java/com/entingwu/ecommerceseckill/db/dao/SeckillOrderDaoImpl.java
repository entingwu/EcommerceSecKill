package com.entingwu.ecommerceseckill.db.dao;

import com.entingwu.ecommerceseckill.db.mappers.SeckillOrderMapper;
import com.entingwu.ecommerceseckill.db.po.SeckillOrder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class SeckillOrderDaoImpl implements SeckillOrderDao {

    @Resource
    private SeckillOrderMapper mapper;

    @Override
    public void insertSeckillOrder(SeckillOrder order) {
        mapper.insert(order);
    }

    @Override
    public SeckillOrder querySeckillOrder(String orderNo) {
        return mapper.selectByOrderNo(orderNo);
    }

    @Override
    public void updateSeckillOrder(SeckillOrder order) {
        mapper.updateByPrimaryKey(order);
    }
}
