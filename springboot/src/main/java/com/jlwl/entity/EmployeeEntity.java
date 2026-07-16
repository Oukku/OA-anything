package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("employee")
public class EmployeeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long yonghuId;
    private Long userId;
    private String empNo;
    private String realName;
    private String englishName;
    private Integer gender;
    private LocalDate birthday;
    private String idCard;
    private String phone;
    private String email;
    private String avatar;
    private Long deptId;
    private Long positionId;
    private Long directManagerId;
    private String employeeType;
    private LocalDate entryDate;
    private LocalDate regularDate;
    private LocalDate leaveDate;
    private LocalDate contractStart;
    private LocalDate contractEnd;
    private String workLocation;
    private Integer status;
    private Integer isExternal;
    @TableLogic
    private Integer delFlag;
    private Long createBy;
    private java.time.LocalDateTime createTime;
    private Long updateBy;
    private java.time.LocalDateTime updateTime;
}
