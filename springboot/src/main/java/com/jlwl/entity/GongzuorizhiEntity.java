package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("gongzuorizhi")
public class GongzuorizhiEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("yonghuming")
    private String username;
    @TableField("dengjishijian")
    private LocalDate rizhiDate;
    @TableField("biaoti")
    private String title;
    @TableField("gongzuoneirong")
    private String content;
    @TableField("addtime")
    private LocalDateTime addtime;
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
    @TableField(exist = false)
    private Long createBy;
    @TableField(exist = false)
    private LocalDateTime createTime;
    @TableField(exist = false)
    private Long updateBy;
    @TableField(exist = false)
    private LocalDateTime updateTime;
}
