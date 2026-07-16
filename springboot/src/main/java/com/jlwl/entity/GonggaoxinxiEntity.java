package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("gonggaoxinxi")
public class GonggaoxinxiEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String fengmian;
    private Integer gonggaoType;
    private Integer fabuRen;
    private Integer status;
    private Integer topFlag;
    private LocalDateTime fabuTime;
    private LocalDateTime endTime;
    private Integer viewCount;
    @TableLogic
    private Integer delFlag;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;
}
