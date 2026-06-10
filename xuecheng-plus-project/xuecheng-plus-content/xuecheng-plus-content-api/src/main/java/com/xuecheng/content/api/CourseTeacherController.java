package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "课程教师管理接口", tags = "课程教师管理接口")
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    @ApiOperation("根据课程id查询教师列表")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> list(@PathVariable Long courseId) {
        return courseTeacherService.listByCourseId(courseId);
    }

    @ApiOperation("新增或修改课程教师")
    @PostMapping("/courseTeacher")
    public CourseTeacher save(@RequestBody CourseTeacher courseTeacher) {
        return courseTeacherService.saveCourseTeacher(courseTeacher);
    }

    @ApiOperation("删除课程教师")
    @DeleteMapping("/courseTeacher/course/{courseId}/{courseTeacherId}")
    public void delete(@PathVariable Long courseId, @PathVariable Long courseTeacherId) {
        courseTeacherService.deleteCourseTeacher(courseId, courseTeacherId);
    }
}
