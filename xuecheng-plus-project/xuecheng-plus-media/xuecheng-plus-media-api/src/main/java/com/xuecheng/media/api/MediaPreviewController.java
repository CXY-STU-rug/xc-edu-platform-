package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(value = "媒资预览（前端无 /open 前缀别名）", tags = "媒资预览别名")
@RestController
public class MediaPreviewController {

    @Autowired
    MediaFileService mediaFileService;

    @ApiOperation("通过媒资id预览")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if (mediaFiles == null) {
            return RestResponse.validfail("找不到视频");
        }
        if (StringUtils.isEmpty(mediaFiles.getUrl())) {
            return RestResponse.validfail("该视频正在处理中");
        }
        return RestResponse.success(mediaFiles.getUrl());
    }
}
