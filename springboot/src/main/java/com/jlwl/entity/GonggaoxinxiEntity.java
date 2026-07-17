package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gonggaoxinxi")
public class GonggaoxinxiEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("biaoti")
    private String title;
    @TableField("gonggaoneirong")
    private String content;
    @TableField("leixing")
    private String gonggaoType;
    @TableField("fabushijian")
    private LocalDateTime fabuTime;
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
