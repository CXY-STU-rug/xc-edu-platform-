package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.config.SeckillConfig;
import com.xuecheng.orders.mapper.SeckillCourseMapper;
import com.xuecheng.orders.mapper.SeckillRecordMapper;
import com.xuecheng.orders.model.po.SeckillCourse;
import com.xuecheng.orders.model.po.SeckillRecord;
import com.xuecheng.orders.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀服务实现。
 * 高并发路径（doSeckill）只碰 Redis 与 MQ，不碰数据库——数据库写入全部由消费端异步完成。
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    public static final String STOCK_KEY_PREFIX = "seckill:stock:";   //库存
    public static final String USERS_KEY_PREFIX = "seckill:users:";   //已抢用户集合
    public static final String INFO_KEY_PREFIX = "seckill:info:";     //活动信息缓存

    @Autowired
    SeckillCourseMapper seckillCourseMapper;
    @Autowired
    SeckillRecordMapper seckillRecordMapper;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    DefaultRedisScript<Long> seckillScript;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void prepareStock(Long activityId) {
        SeckillCourse activity = seckillCourseMapper.selectById(activityId);
        if (activity == null) {
            XueChengPlusException.cast("秒杀活动不存在");
        }
        if (!"1".equals(activity.getStatus())) {
            XueChengPlusException.cast("活动未上架");
        }
        //库存与活动信息写入 Redis；重新预热视为重置活动（清掉已购用户集合）
        redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + activityId, String.valueOf(activity.getStock()));
        redisTemplate.opsForValue().set(INFO_KEY_PREFIX + activityId, JSON.toJSONString(activity));
        redisTemplate.delete(USERS_KEY_PREFIX + activityId);
        log.info("秒杀活动 {} 预热完成, 库存 {}", activityId, activity.getStock());
    }

    @Override
    public String doSeckill(String userId, Long activityId) {
        //1. 活动信息走 Redis（预热时写入），时间窗校验不打数据库
        String infoJson = redisTemplate.opsForValue().get(INFO_KEY_PREFIX + activityId);
        if (infoJson == null) {
            XueChengPlusException.cast("活动不存在或未开始");
        }
        SeckillCourse activity = JSON.parseObject(infoJson, SeckillCourse.class);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            XueChengPlusException.cast("活动尚未开始");
        }
        if (now.isAfter(activity.getEndTime())) {
            XueChengPlusException.cast("活动已结束");
        }

        //2. Lua 原子执行：防重复 + 库存校验 + 扣减（详见 lua/seckill.lua 返回值约定）
        Long result = redisTemplate.execute(seckillScript,
                Arrays.asList(STOCK_KEY_PREFIX + activityId, USERS_KEY_PREFIX + activityId),
                userId);
        if (result == null || result == -2) {
            XueChengPlusException.cast("活动不存在或未开始");
        }
        if (result == -1) {
            XueChengPlusException.cast("您已抢购过该课程，请勿重复抢购");
        }
        if (result == 0) {
            XueChengPlusException.cast("名额已抢光");
        }

        //3. 抢到名额：发 MQ 异步落库下单，接口立即返回（前端轮询结果）
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", userId);
        msg.put("activityId", activityId);
        try {
            rabbitTemplate.convertAndSend(SeckillConfig.SECKILL_EXCHANGE,
                    SeckillConfig.SECKILL_ROUTING_KEY, JSON.toJSONString(msg));
        } catch (Exception e) {
            //MQ 投递失败必须回补名额，否则名额被占却永远不会有订单
            log.error("秒杀消息投递失败, 回补库存, userId:{}, activityId:{}", userId, activityId, e);
            redisTemplate.opsForValue().increment(STOCK_KEY_PREFIX + activityId);
            redisTemplate.opsForSet().remove(USERS_KEY_PREFIX + activityId, userId);
            XueChengPlusException.cast("系统繁忙，请稍后再试");
        }
        return "抢购成功，订单生成中，请稍后查询结果";
    }

    @Override
    public SeckillRecord queryResult(String userId, Long activityId) {
        //1. 先查数据库记录（消费端已落库 → 排队中/成功/失败 状态都在这）
        SeckillRecord record = seckillRecordMapper.selectOne(new LambdaQueryWrapper<SeckillRecord>()
                .eq(SeckillRecord::getActivityId, activityId)
                .eq(SeckillRecord::getUserId, userId));
        if (record != null) {
            return record;
        }
        //2. 数据库没有但 Redis 已记录该用户 → 消息还在队列里，返回排队中占位
        Boolean inQueue = redisTemplate.opsForSet().isMember(USERS_KEY_PREFIX + activityId, userId);
        if (Boolean.TRUE.equals(inQueue)) {
            SeckillRecord queueing = new SeckillRecord();
            queueing.setActivityId(activityId);
            queueing.setUserId(userId);
            queueing.setStatus(SeckillRecord.STATUS_QUEUEING);
            return queueing;
        }
        //3. 两边都没有 → 未参与
        return null;
    }
}
