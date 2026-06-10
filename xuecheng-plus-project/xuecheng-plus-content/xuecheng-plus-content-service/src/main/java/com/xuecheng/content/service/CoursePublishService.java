package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * @version 1.0
 * @description 课程发布相关的接口
 */
public interface CoursePublishService {


 /**
  * @description 获取课程预览信息
  * @param courseId 课程id
  * @return com.xuecheng.content.model.dto.CoursePreviewDto
  */
 public CoursePreviewDto getCoursePreviewInfo(Long courseId);

 /**
  * @description 提交审核
  * @param courseId  课程id
  * @return void
  */
 public void commitAudit(Long companyId,Long courseId);

 /**
  * @description 课程发布接口
  * @param companyId 机构id
  * @param courseId 课程id
  * @return void
  */
 public void publish(Long companyId,Long courseId);

 /**
  * @description 课程静态化
  * @param courseId  课程id
  * @return File 静态化文件
  */
 public File generateCourseHtml(Long courseId);
 /**
  * @description 上传课程静态化页面
  * @param file  静态化文件
  * @return void
  */
 public void  uploadCourseHtml(Long courseId, File file);

 /**
  * 根据课程 id查询课程发布信息
  * @param courseId
  * @return
  */
 public CoursePublish getCoursePublish(Long courseId);

 /**
  * 查询课程发布信息（走 Redis 缓存，未命中回源数据库并回填）
  * 含空值缓存防穿透、互斥锁防击穿、过期时间打散防雪崩
  * @param courseId 课程id
  * @return 课程发布信息，课程不存在返回 null
  */
 public CoursePublish getCoursePublishCache(Long courseId);

 /**
  * 课程发布后预热 Redis 缓存（xxl-job 任务第三阶段调用）
  * @param courseId 课程id
  */
 public void saveCourseCache(Long courseId);
}
