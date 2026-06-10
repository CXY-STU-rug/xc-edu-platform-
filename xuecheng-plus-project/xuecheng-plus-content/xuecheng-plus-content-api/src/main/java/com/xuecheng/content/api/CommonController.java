package com.xuecheng.content.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Api(value = "公共接口", tags = "公共接口")
@RestController
@RequestMapping("/common")
public class CommonController {

    @ApiOperation("发送短信验证码（占位实现，固定 666666）")
    @GetMapping("/smsMsg")
    public Map<String, Object> smsMsg(@RequestParam(required = false) String phone) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("code", "666666");
        ret.put("phone", phone);
        ret.put("expire", 300);
        return ret;
    }

    @ApiOperation("七牛上传 token（占位实现）")
    @PostMapping("/qnUploadToken")
    public Map<String, Object> qnUploadToken(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("token", "stub-qn-token-" + UUID.randomUUID());
        ret.put("uploadDomain", "https://upload-z2.qiniup.com");
        ret.put("downloadDomain", "http://file.51xuecheng.cn");
        return ret;
    }
}
