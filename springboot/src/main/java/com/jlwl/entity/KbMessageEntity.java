package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("kb_message")
public class KbMessageEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("session_id")
    private Long sessionId;
    @TableField("role")
    private String role;
    @TableField("content")
    private String content;
    @TableField("mode")
    private String mode;
    @TableField("duration_ms")
    private Integer durationMs;
    @TableField("sources")
    private String sources;
    @TableField("create_time")
    private LocalDateTime createTime;
}
