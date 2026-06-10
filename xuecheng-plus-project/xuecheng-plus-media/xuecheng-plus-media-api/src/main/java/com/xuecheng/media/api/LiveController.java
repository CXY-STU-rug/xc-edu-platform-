package com.xuecheng.media.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "直播管理接口", tags = "直播管理接口")
@RestController
@RequestMapping("/live")
public class LiveController {

    @ApiOperation("直播课程列表（占位返回空列表）")
    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("items", Collections.emptyList());
        ret.put("counts", 0);
        ret.put("page", 1);
        ret.put("pageSize", 20);
        return ret;
    }

    @ApiOperation("直播推流地址（占位返回 mock URL）")
    @GetMapping("/pushUrl/{liveId}")
    public Map<String, Object> pushUrl(@PathVariable String liveId) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("liveId", liveId);
        ret.put("pushUrl", "rtmp://live-push.example.com/live/" + liveId + "?stub=1");
        ret.put("playUrl", "rtmp://live-pull.example.com/live/" + liveId);
        return ret;
    }
}
