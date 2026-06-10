package com.xuecheng.orders.service;

import com.xuecheng.orders.model.po.SeckillRecord;

/**
 * 限时秒杀课服务
 */
public interface SeckillService {

    /**
     * 活动库存预热：把 seckill_course 的名额与活动信息写入 Redis（活动开始前由运营触发）
     * @param activityId 活动id
     */
    void prepareStock(Long activityId);

    /**
     * 执行秒杀：Lua 原子扣减 Redis 库存，成功后发 MQ 异步生成订单，接口本身不写数据库
     * @return 提示信息（排队中）
     */
    String doSeckill(String userId, Long activityId);

    /**
     * 轮询秒杀结果
     * @return 秒杀记录（status: 1排队中 2成功待支付 3失败），未参与返回 null
     */
    SeckillRecord queryResult(String userId, Long activityId);
}
