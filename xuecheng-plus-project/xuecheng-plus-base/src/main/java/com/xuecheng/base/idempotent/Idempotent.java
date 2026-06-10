package com.xuecheng.base.idempotent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口幂等注解：在指定时间窗口内，相同业务键的请求只允许执行一次。
 * 实现原理：Redis setIfAbsent（SETNX + 过期时间原子操作）抢占标记，
 * 抢不到说明窗口内已有相同请求，直接拒绝；业务执行失败则释放标记允许重试。
 *
 * 使用示例：
 * {@code @Idempotent(keyPrefix = "order:create", key = "#addOrderDto.outBusinessId", expireSeconds = 30)}
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * Redis key 前缀，建议按业务命名（如 order:create），避免不同接口互相干扰
     */
    String keyPrefix() default "idempotent";

    /**
     * 业务键的 SpEL 表达式，从方法参数中提取（如 "#addOrderDto.outBusinessId"）
     */
    String key();

    /**
     * 幂等窗口时长（秒）：窗口内相同业务键的请求被拒绝
     */
    long expireSeconds() default 60;

    /**
     * 触发幂等拦截时返回给前端的提示语
     */
    String message() default "请求正在处理或重复提交，请稍后再试";
}
