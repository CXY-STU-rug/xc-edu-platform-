package com.xuecheng.learning.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.learning.mapper.XcLearnRecordMapper;
import com.xuecheng.learning.model.po.XcLearnRecord;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "学习记录接口", tags = "学习记录接口")
@RestController
@RequestMapping("/learnedRecords")
public class LearnRecordController {

    @Autowired
    XcLearnRecordMapper xcLearnRecordMapper;

    @ApiOperation("学习记录列表")
    @GetMapping("/list")
    public Map<String, Object> list(@RequestParam(required = false) String userId,
                                    @RequestParam(required = false) Long courseId) {
        LambdaQueryWrapper<XcLearnRecord> w = new LambdaQueryWrapper<>();
        if (userId != null && !userId.isEmpty()) w.eq(XcLearnRecord::getUserId, userId);
        if (courseId != null) w.eq(XcLearnRecord::getCourseId, courseId);
        w.orderByDesc(XcLearnRecord::getLearnDate);
        List<XcLearnRecord> items = xcLearnRecordMapper.selectList(w);
        Map<String, Object> ret = new HashMap<>();
        ret.put("items", items);
        ret.put("counts", items.size());
        return ret;
    }
}
