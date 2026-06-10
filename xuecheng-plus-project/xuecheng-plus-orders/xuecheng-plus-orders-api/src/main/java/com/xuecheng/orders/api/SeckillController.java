package com.xuecheng.orders.api;

import com.xuecheng.base.idempotent.Idempotent;
import com.xuecheng.orders.model.po.SeckillRecord;
import com.xuecheng.orders.service.SeckillService;
import com.xuecheng.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 限时秒杀课接口。
 * 抢购接口高并发路径只操作 Redis 与 MQ；订单由消费端异步生成，前端轮询查询结果。
 */
@Api(value = "秒杀课接口", tags = "秒杀课接口")
@RestController
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @ApiOperation("活动库存预热（运营操作, 活动开始前执行）")
    @PostMapping("/seckill/prepare/{activityId}")
    public void prepare(@PathVariable("activityId") Long activityId) {
        seckillService.prepareStock(activityId);
    }

    @ApiOperation("执行秒杀")
    @PostMapping("/seckill/{activityId}")
    //幂等兜底：同一用户对同一活动 5 秒内只放一个请求进来，挡住前端连点（Lua 的 sismember 是业务级防重）
    @Idempotent(keyPrefix = "seckill:req", key = "#activityId + ':' + T(com.xuecheng.orders.util.SecurityUtil).getUser().getId()", expireSeconds = 5, message = "请求过于频繁，请稍后再试")
    public String seckill(@PathVariable("activityId") Long activityId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        return seckillService.doSeckill(user.getId(), activityId);
    }

    @ApiOperation("轮询秒杀结果")
    @GetMapping("/seckill/result/{activityId}")
    public SeckillRecord queryResult(@PathVariable("activityId") Long activityId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        return seckillService.queryResult(user.getId(), activityId);
    }
}
