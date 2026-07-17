package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("kb")
public class KbEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("name")
    private String name;
    @TableField("code")
    private String code;
    @TableField("description")
    private String description;
    @TableField("status")
    private Integer status;
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
    @TableField("create_by")
    private Long createBy;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_by")
    private Long updateBy;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
