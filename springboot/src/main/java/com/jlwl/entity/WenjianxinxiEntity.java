package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wenjianxinxi")
public class WenjianxinxiEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String url;
    private String fileType;
    private Long fileSize;
    private Long folderId;
    private String tags;
    private String description;
    private Integer accessLevel;
    private Integer status;
    @TableLogic
    private Integer delFlag;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
