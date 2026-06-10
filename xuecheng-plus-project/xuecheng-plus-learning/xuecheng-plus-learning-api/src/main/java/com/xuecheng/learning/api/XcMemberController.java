package com.xuecheng.learning.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Api(value = "会员管理接口", tags = "会员管理接口")
@RestController
@RequestMapping("/xc-member")
public class XcMemberController {

    @ApiOperation("会员列表（占位）")
    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("items", Collections.emptyList());
        ret.put("counts", 0);
        return ret;
    }

    @ApiOperation("按手机号查询会员（占位）")
    @GetMapping("/get-by-phone")
    public Map<String, Object> getByPhone(@RequestParam String phone) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("phone", phone);
        ret.put("found", false);
        return ret;
    }

    @ApiOperation("绑定会员")
    @PostMapping("/binding")
    public Map<String, Object> binding(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("success", true);
        ret.put("message", "绑定成功（占位）");
        ret.put("payload", body);
        return ret;
    }

    @ApiOperation("解绑会员")
    @DeleteMapping("/unbinding/{memberId}")
    public Map<String, Object> unbinding(@PathVariable String memberId) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("success", true);
        ret.put("memberId", memberId);
        return ret;
    }
}
