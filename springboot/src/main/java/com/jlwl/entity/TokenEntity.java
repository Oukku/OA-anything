package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("token")
public class TokenEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String userType;
    private String username;
    private String token;
    private LocalDateTime expireTime;
    private String ip;
    private LocalDateTime createTime;
}
