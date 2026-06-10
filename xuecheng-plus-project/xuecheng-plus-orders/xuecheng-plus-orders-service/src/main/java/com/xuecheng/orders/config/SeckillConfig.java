package com.xuecheng.orders.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * 秒杀相关基础设施：MQ 队列（异步下单削峰）+ Lua 脚本 Bean（原子扣库存）
 */
@Configuration
public class SeckillConfig {

    //秒杀下单交换机与队列：direct 点对点即可，无需广播
    public static final String SECKILL_EXCHANGE = "seckill_order_exchange";
    public static final String SECKILL_QUEUE = "seckill_order_queue";
    public static final String SECKILL_ROUTING_KEY = "seckill.order";

    @Bean(SECKILL_EXCHANGE)
    public DirectExchange seckillExchange() {
        return new DirectExchange(SECKILL_EXCHANGE, true, false);
    }

    @Bean(SECKILL_QUEUE)
    public Queue seckillQueue() {
        return QueueBuilder.durable(SECKILL_QUEUE).build();
    }

    @Bean
    public Binding seckillBinding(@Qualifier(SECKILL_QUEUE) Queue queue,
                                  @Qualifier(SECKILL_EXCHANGE) DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(SECKILL_ROUTING_KEY);
    }

    /**
     * 秒杀 Lua 脚本：启动时从 classpath 加载一次，执行时 Redis 走 EVALSHA 缓存
     */
    @Bean
    public DefaultRedisScript<Long> seckillScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/seckill.lua")));
        script.setResultType(Long.class);
        return script;
    }
}
