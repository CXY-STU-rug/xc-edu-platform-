package com.xuecheng.base.trace;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * traceId 过滤器：请求进入时从请求头取 traceId（网关生成并透传），
 * 没有则自己生成，放入 MDC 供 log4j2 的 %X{traceId} 输出，响应结束后清理。
 * 仅在 Servlet Web 环境注册（网关是 WebFlux，自动跳过，避免类加载失败）。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) //最先执行，保证后续所有日志都带 traceId
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class TraceFilter implements Filter {

    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String TRACE_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        //优先取上游（网关/调用方）透传的 traceId，保证同一请求全链路一个 id
        String traceId = req.getHeader(TRACE_HEADER);
        if (StringUtils.isEmpty(traceId)) {
            //直接打到本服务（绕过网关）的请求自己生成，取 UUID 前 16 位够用且短
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        MDC.put(TRACE_KEY, traceId);
        //回写响应头：前端报障时直接报 traceId，后端能秒定位整条链路日志
        ((HttpServletResponse) response).setHeader(TRACE_HEADER, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            //必须清理：Tomcat 线程池复用线程，不清理会串到下一个请求
            MDC.remove(TRACE_KEY);
        }
    }
}
