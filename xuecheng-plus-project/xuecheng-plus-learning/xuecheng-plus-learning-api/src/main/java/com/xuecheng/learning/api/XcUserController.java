package com.xuecheng.learning.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Api(value = "用户中心接口（占位）", tags = "用户中心接口")
@RestController
@RequestMapping("/xcUser")
public class XcUserController {

    @ApiOperation("新增用户（占位）")
    @PostMapping
    public Map<String, Object> create(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("success", true);
        ret.put("payload", body);
        return ret;
    }

    @ApiOperation("修改用户（占位）")
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable String id,
                                      @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("success", true);
        ret.put("id", id);
        ret.put("payload", body);
        return ret;
    }

    @ApiOperation("修改密码：旧密码校验（占位，固定通过）")
    @PostMapping("/changePwd/verify")
    public Map<String, Object> verifyOldPwd(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("verified", true);
        return ret;
    }

    @ApiOperation("修改密码：新密码（占位）")
    @PutMapping("/changePwd/{id}")
    public Map<String, Object> changePwd(@PathVariable String id,
                                         @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("success", true);
        ret.put("id", id);
        return ret;
    }
}
