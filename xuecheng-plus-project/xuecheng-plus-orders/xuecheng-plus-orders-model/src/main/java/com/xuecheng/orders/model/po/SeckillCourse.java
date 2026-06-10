package com.xuecheng.orders.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动
 */
@Data
@TableName("seckill_course")
public class SeckillCourse implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 课程id（content 库 course_publish.id） */
    private Long courseId;

    /** 课程名称（冗余存储，避免跨库查询） */
    private String courseName;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 秒杀价 */
    private BigDecimal seckillPrice;

    /** 秒杀名额（数据库侧底账，运行时以 Redis 为准） */
    private Integer stock;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 状态 1上架 0下架 */
    private String status;

    private LocalDateTime createDate;
}
