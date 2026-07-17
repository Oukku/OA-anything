package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("kb_session")
public class KbSessionEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("kb_id")
    private Long kbId;
    @TableField("title")
    private String title;
    @TableField("status")
    private Integer status;
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
