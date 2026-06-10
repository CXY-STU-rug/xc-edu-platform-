package com.xuecheng.orders.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀记录：activity_id + user_id 唯一键，
 * 既防同一用户重复抢购，也是 MQ 消费端幂等写入的依据。
 */
@Data
@TableName("seckill_record")
public class SeckillRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 排队中（已抢到名额，订单生成中） */
    public static final String STATUS_QUEUEING = "1";
    /** 成功，订单已生成待支付 */
    public static final String STATUS_SUCCESS = "2";
    /** 失败（下单异常，名额已回补） */
    public static final String STATUS_FAIL = "3";

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 秒杀活动id */
    private Long activityId;

    /** 课程id */
    private Long courseId;

    /** 用户id */
    private String userId;

    /** 成交秒杀价 */
    private BigDecimal seckillPrice;

    /** 生成的订单id */
    private Long orderId;

    /** 支付记录号（前端拿它去支付） */
    private String payNo;

    /** 状态：1排队中 2成功待支付 3失败 */
    private String status;

    private LocalDateTime createDate;
}
