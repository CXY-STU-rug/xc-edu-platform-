package com.xuecheng.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 网关 traceId 全局过滤器：作为流量唯一入口，为每个请求生成 traceId
 * 并写入 X-Trace-Id 请求头透传给下游服务（下游 TraceFilter 读取后放 MDC），
 * 同时回写到响应头，前端报障时直接提供 traceId 即可串起整条链路日志。
 */
@Component
public class TraceGlobalFilter implements GlobalFilter, Ordered {

    public static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //已带 traceId 的请求（如内部转发）不重复生成
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        //WebFlux 的请求对象不可变，mutate 出带 traceId 头的新请求
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(TRACE_HEADER, traceId)
                .build();
        exchange.getResponse().getHeaders().set(TRACE_HEADER, traceId);
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        //优先级高于鉴权等过滤器，保证全链路尽早拿到 traceId
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
