package com.xuecheng.content.model.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("teachplan_work")
public class TeachplanWork implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long workId;

    private String workTitle;

    private Long teachplanId;

    private Long courseId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate;

    private Long coursePubId;
}
