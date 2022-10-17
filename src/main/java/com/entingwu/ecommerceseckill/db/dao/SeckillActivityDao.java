package com.entingwu.ecommerceseckill.db.dao;

import com.entingwu.ecommerceseckill.db.po.SeckillActivity;

import java.util.List;

public interface SeckillActivityDao {

    public List<SeckillActivity> querySeckillActivitysByStatus(int activityStatus);

    public void insertSeckillActivity(SeckillActivity seckillActivity);

    public SeckillActivity querySeckillActivityById(long activityId);

    public void updateSeckillActivity(SeckillActivity seckillActivity);

    boolean lockStock(long seckillActivityId);

    boolean deductStock(Long seckillActivityId);

    void revertStock(Long seckillActivityId);
}
