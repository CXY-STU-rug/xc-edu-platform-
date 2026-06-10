package com.xuecheng.media.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Api(value = "媒资点播扩展接口", tags = "媒资点播扩展接口")
@RestController
@RequestMapping("/media")
public class MediaQnController {

    @ApiOperation("媒资列表（占位返回空列表）")
    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) Integer pageNo,
                                    @RequestParam(required = false) Integer pageSize) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("items", Collections.emptyList());
        ret.put("counts", 0);
        ret.put("page", pageNo != null ? pageNo : 1);
        ret.put("pageSize", pageSize != null ? pageSize : 20);
        return ret;
    }

    @ApiOperation("七牛上传参数（占位）")
    @PostMapping("/qn-params")
    public Map<String, Object> qnParams(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("token", "stub-qn-params-" + UUID.randomUUID());
        ret.put("key", body != null ? body.getOrDefault("key", "stub.dat") : "stub.dat");
        ret.put("uploadDomain", "https://upload-z2.qiniup.com");
        ret.put("downloadDomain", "http://file.51xuecheng.cn");
        return ret;
    }

    @ApiOperation("阿里云 VOD 上传 token（占位）")
    @GetMapping("/vod-token")
    public Map<String, Object> vodToken(@RequestParam(required = false) String fileName) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("requestId", UUID.randomUUID().toString());
        ret.put("videoId", "stub-vid-" + System.currentTimeMillis());
        ret.put("uploadAddress", "stub-upload-address-base64");
        ret.put("uploadAuth", "stub-upload-auth-base64");
        ret.put("fileName", fileName);
        return ret;
    }
}
