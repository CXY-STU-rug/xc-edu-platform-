package com.xuecheng.system.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(value = "系统配置接口", tags = "系统配置接口")
@RestController
public class SystemsController {

    @ApiOperation("系统通用配置（占位）")
    @GetMapping("/systems")
    public Map<String, Object> systems() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("siteName", "学成在线");
        ret.put("fileServer", "http://file.51xuecheng.cn");
        ret.put("uploadDomain", "https://upload-z2.qiniup.com");
        ret.put("env", "dev");
        return ret;
    }
}
