package com.xuecheng.content.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanWorkMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanWork;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 课程计划管理相关的接口
 * @date 2023/2/14 11:25
 */
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    TeachplanWorkMapper teachplanWorkMapper;

   @ApiOperation("查询课程计划树形结构")
   //查询课程计划  GET /teachplan/22/tree-nodes
   @GetMapping("/teachplan/{courseId}/tree-nodes")
 public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
       List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);

       return teachplanTree;
   }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation("课程计划删除")
    @DeleteMapping("/teachplan/{teachplanId}")
    public void deleteTeachplan(@PathVariable Long teachplanId) {
        teachplanService.deleteTeachplan(teachplanId);
    }

    @ApiOperation("课程计划上移")
    @PostMapping("/teachplan/moveup/{teachplanId}")
    public void moveup(@PathVariable Long teachplanId) {
        teachplanService.moveup(teachplanId);
    }

    @ApiOperation("课程计划下移")
    @PostMapping("/teachplan/movedown/{teachplanId}")
    public void movedown(@PathVariable Long teachplanId) {
        teachplanService.movedown(teachplanId);
    }

    @ApiOperation("查询课程作业绑定")
    @GetMapping("/teachplan/work/{teachplanId}")
    public TeachplanWork getWork(@PathVariable Long teachplanId) {
        return teachplanWorkMapper.selectOne(
                new LambdaQueryWrapper<TeachplanWork>().eq(TeachplanWork::getTeachplanId, teachplanId));
    }

    @ApiOperation("课程作业绑定")
    @PostMapping("/teachplan/work/association")
    public TeachplanWork associationWork(@RequestBody TeachplanWork req) {
        TeachplanWork exist = teachplanWorkMapper.selectOne(
                new LambdaQueryWrapper<TeachplanWork>().eq(TeachplanWork::getTeachplanId, req.getTeachplanId()));
        if (exist != null) {
            req.setId(exist.getId());
            teachplanWorkMapper.updateById(req);
        } else {
            teachplanWorkMapper.insert(req);
        }
        return req;
    }

    @ApiOperation("解除课程作业绑定")
    @DeleteMapping("/teachplan/work/{teachplanWorkId}")
    public void deleteWork(@PathVariable Long teachplanWorkId) {
        teachplanWorkMapper.deleteById(teachplanWorkId);
    }

}
