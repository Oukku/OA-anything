package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("wenjianxinxi")
public class WenjianxinxiEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("biaoti")
    private String name;
    @TableField("fujian")
    private String url;
    @TableField("wenjianneirong")
    private String description;
    @TableField("fabushijian")
    private LocalDateTime fabuTime;
    @TableField("addtime")
    private LocalDateTime addtime;
    @TableField("sfsh")
    private String sfsh;        // 审核状态: 是=已通过, 否=待审核, 驳回=已驳回
    @TableField("shhf")
    private String shhf;        // 审核回复
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
