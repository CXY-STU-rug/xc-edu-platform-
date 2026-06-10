package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> listByCourseId(Long courseId) {
        return courseTeacherMapper.selectList(
                new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, courseId));
    }

    @Override
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher) {
        if (courseTeacher.getCourseId() == null) {
            XueChengPlusException.cast("课程id不能为空");
        }
        if (courseTeacher.getId() == null) {
            courseTeacher.setCreateDate(LocalDateTime.now());
            courseTeacherMapper.insert(courseTeacher);
        } else {
            courseTeacherMapper.updateById(courseTeacher);
        }
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long courseTeacherId) {
        CourseTeacher exist = courseTeacherMapper.selectOne(
                new LambdaQueryWrapper<CourseTeacher>()
                        .eq(CourseTeacher::getCourseId, courseId)
                        .eq(CourseTeacher::getId, courseTeacherId));
        if (exist == null) {
            XueChengPlusException.cast("教师信息不存在或不属于该课程");
        }
        courseTeacherMapper.deleteById(courseTeacherId);
    }
}
