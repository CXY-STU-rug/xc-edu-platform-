-- v2.3 限时秒杀课 建表脚本（执行于 xcplus_orders 库）
USE xcplus_orders;

-- 秒杀活动表
CREATE TABLE IF NOT EXISTS `seckill_course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '活动id',
  `course_id` BIGINT NOT NULL COMMENT '课程id（content 库 course_publish.id）',
  `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称（冗余，避免跨库查询）',
  `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
  `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
  `stock` INT NOT NULL COMMENT '秒杀名额（库存）',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `status` VARCHAR(10) NOT NULL DEFAULT '1' COMMENT '状态 1上架 0下架',
  `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动';

-- 秒杀成功记录表（唯一键防同一用户重复抢购，兼做 MQ 消费幂等）
CREATE TABLE IF NOT EXISTS `seckill_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL COMMENT '秒杀活动id',
  `course_id` BIGINT NOT NULL COMMENT '课程id',
  `user_id` VARCHAR(60) NOT NULL COMMENT '用户id',
  `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '成交秒杀价',
  `order_id` BIGINT NULL COMMENT '生成的订单id',
  `pay_no` VARCHAR(60) NULL COMMENT '支付记录号',
  `status` VARCHAR(10) NOT NULL DEFAULT '1' COMMENT '1排队中 2成功待支付 3失败',
  `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀记录';

-- 演示活动：对课程 id=2（按实际已发布课程调整），100 个名额
INSERT INTO `seckill_course` (course_id, course_name, original_price, seckill_price, stock, start_time, end_time, status)
VALUES (2, '测试课程-限时秒杀', 199.00, 9.90, 100, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), '1');
