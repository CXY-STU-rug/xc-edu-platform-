package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.orders.config.SeckillConfig;
import com.xuecheng.orders.mapper.SeckillCourseMapper;
import com.xuecheng.orders.mapper.SeckillRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.SeckillCourse;
import com.xuecheng.orders.model.po.SeckillRecord;
import com.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 秒杀下单消费端：从 MQ 取出抢购成功消息，写秒杀记录并复用订单服务生成待支付订单。
 * 幂等依据：seckill_record 的 (activity_id, user_id) 唯一键 + 先查后插。
 */
@Slf4j
@Component
public class SeckillOrderConsumer {

    @Autowired
    SeckillRecordMapper seckillRecordMapper;
    @Autowired
    SeckillCourseMapper seckillCourseMapper;
    @Autowired
    OrderService orderService;
    @Autowired
    StringRedisTemplate redisTemplate;

    @RabbitListener(queues = SeckillConfig.SECKILL_QUEUE)
    public void onSeckillMessage(String message) {
        JSONObject msg = JSON.parseObject(message);
        String userId = msg.getString("userId");
        Long activityId = msg.getLong("activityId");

        //1. 幂等检查：MQ 可能重复投递，已有记录直接跳过
        SeckillRecord exist = seckillRecordMapper.selectOne(new LambdaQueryWrapper<SeckillRecord>()
                .eq(SeckillRecord::getActivityId, activityId)
                .eq(SeckillRecord::getUserId, userId));
        if (exist != null) {
            log.debug("秒杀记录已存在, 跳过, userId:{}, activityId:{}", userId, activityId);
            return;
        }

        SeckillCourse activity = seckillCourseMapper.selectById(activityId);
        if (activity == null) {
            log.error("秒杀活动 {} 不存在, 消息丢弃", activityId);
            return;
        }

        //2. 写秒杀记录（排队中）；并发重复消费时唯一索引兜底
        SeckillRecord record = new SeckillRecord();
        record.setActivityId(activityId);
        record.setCourseId(activity.getCourseId());
        record.setUserId(userId);
        record.setSeckillPrice(activity.getSeckillPrice());
        record.setStatus(SeckillRecord.STATUS_QUEUEING);
        try {
            seckillRecordMapper.insert(record);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.debug("唯一键冲突说明已被其他消费者处理, 跳过");
            return;
        }

        //3. 复用订单服务生成待支付订单（outBusinessId 用唯一业务键, createOrder 内部按它幂等）
        try {
            AddOrderDto addOrderDto = new AddOrderDto();
            addOrderDto.setTotalPrice(activity.getSeckillPrice().floatValue());
            addOrderDto.setOrderType("60201"); //课程购买
            addOrderDto.setOrderName("[秒杀]" + activity.getCourseName());
            addOrderDto.setOrderDescrip("限时秒杀课程:" + activity.getCourseName());
            //订单明细与普通购课保持同构, learning 消费支付结果时无差别处理
            JSONObject goods = new JSONObject();
            goods.put("goodsId", String.valueOf(activity.getCourseId()));
            goods.put("goodsType", "60201");
            goods.put("goodsName", activity.getCourseName());
            goods.put("goodsPrice", activity.getSeckillPrice());
            addOrderDto.setOrderDetail("[" + goods.toJSONString() + "]");
            addOrderDto.setOutBusinessId("seckill:" + activityId + ":" + userId);

            PayRecordDto payRecord = orderService.createOrder(userId, addOrderDto);

            //4. 回填订单信息，状态置成功待支付（payNo 在 PO 里是 Long, 记录表存 String）
            record.setOrderId(payRecord.getOrderId());
            record.setPayNo(payRecord.getPayNo() == null ? null : String.valueOf(payRecord.getPayNo()));
            record.setStatus(SeckillRecord.STATUS_SUCCESS);
            seckillRecordMapper.updateById(record);
            log.info("秒杀订单生成成功, userId:{}, activityId:{}, payNo:{}", userId, activityId, payRecord.getPayNo());
        } catch (Exception e) {
            //5. 下单失败：记录失败状态并回补 Redis 名额, 让别人还能抢
            log.error("秒杀订单生成失败, 回补名额, userId:{}, activityId:{}", userId, activityId, e);
            record.setStatus(SeckillRecord.STATUS_FAIL);
            seckillRecordMapper.updateById(record);
            redisTemplate.opsForValue().increment(SeckillServiceImpl.STOCK_KEY_PREFIX + activityId);
            redisTemplate.opsForSet().remove(SeckillServiceImpl.USERS_KEY_PREFIX + activityId, userId);
        }
    }
}
