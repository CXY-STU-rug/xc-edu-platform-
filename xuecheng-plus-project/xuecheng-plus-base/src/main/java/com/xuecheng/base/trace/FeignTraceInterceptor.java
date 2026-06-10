package com.xuecheng.base.trace;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * Feign 拦截器：把当前线程 MDC 中的 traceId 续传到下游服务的请求头，
 * 下游的 TraceFilter 读到后放进自己的 MDC，实现跨服务日志串联。
 *
 * 已知局限：Hystrix 默认线程池隔离时，本拦截器运行在 Hystrix 线程，
 * MDC(ThreadLocal) 取不到调用线程的 traceId，链路在此断开（不报错，下游自生成新 id）。
 * v2.2 迁移 Sentinel（信号量隔离，不切换线程）后该问题自然消除。
 */
@Component
@ConditionalOnClass(name = "feign.RequestInterceptor") //没有引入 Feign 的服务自动跳过
public class FeignTraceInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String traceId = MDC.get(TraceFilter.TRACE_KEY);
        if (traceId != null) {
            template.header(TraceFilter.TRACE_HEADER, traceId);
        }
    }
}
