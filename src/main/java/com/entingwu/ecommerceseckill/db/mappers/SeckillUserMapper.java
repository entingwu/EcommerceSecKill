package com.entingwu.ecommerceseckill.db.mappers;

import com.entingwu.ecommerceseckill.db.po.SeckillUser;

public interface SeckillUserMapper {
    int deleteByPrimaryKey(Long id);

    int insert(SeckillUser record);

    int insertSelective(SeckillUser record);

    SeckillUser selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SeckillUser record);

    int updateByPrimaryKey(SeckillUser record);
}