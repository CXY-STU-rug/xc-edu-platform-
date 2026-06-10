package com.xuecheng.base.idempotent;

import com.xuecheng.base.exception.XueChengPlusException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 幂等注解切面。
 * 条件装配：类路径存在 StringRedisTemplate（即服务引入了 data-redis 依赖）才注册本切面，
 * 未引入 Redis 的服务不受影响，避免 base 模块绑架所有服务的依赖。
 */
@Slf4j
@Aspect
@Component
@ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
public class IdempotentAspect {

    @Autowired
    StringRedisTemplate redisTemplate;

    //SpEL 解析器与参数名发现器都是线程安全的，可复用
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint pjp, Idempotent idempotent) throws Throwable {
        //1. 从方法参数解析出业务键（如订单的 outBusinessId）
        String bizKey = parseBizKey(idempotent.key(), pjp);
        if (bizKey == null || bizKey.isEmpty()) {
            //业务键缺失时放行，交给参数校验去拦——幂等组件只负责防重，不替代校验
            log.warn("幂等业务键解析为空, 跳过幂等检查: {}", idempotent.key());
            return pjp.proceed();
        }
        String redisKey = idempotent.keyPrefix() + ":" + bizKey;

        //2. SETNX + TTL 原子抢占：成功=首个请求, 失败=窗口内重复请求
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofSeconds(idempotent.expireSeconds()));
        if (acquired == null || !acquired) {
            log.info("幂等拦截重复请求, key: {}", redisKey);
            XueChengPlusException.cast(idempotent.message());
        }

        try {
            //3. 首个请求正常执行业务
            return pjp.proceed();
        } catch (Throwable t) {
            //4. 业务失败删除标记：幂等只拦"重复提交"，不能拦"失败后的重试"
            redisTemplate.delete(redisKey);
            throw t;
        }
    }

    /**
     * 用 SpEL 从方法参数中取业务键，例如 key="#addOrderDto.outBusinessId"
     */
    private String parseBizKey(String spel, ProceedingJoinPoint pjp) {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            //拿到参数名数组（如 ["addOrderDto"]），与参数值一一对应注入 SpEL 上下文
            String[] paramNames = nameDiscoverer.getParameterNames(method);
            Object[] args = pjp.getArgs();
            StandardEvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            Object value = parser.parseExpression(spel).getValue(context);
            return value == null ? null : value.toString();
        } catch (Exception e) {
            //表达式写错不应让业务接口不可用，记录后跳过幂等
            log.error("幂等 SpEL 解析失败: {}", spel, e);
            return null;
        }
    }
}
