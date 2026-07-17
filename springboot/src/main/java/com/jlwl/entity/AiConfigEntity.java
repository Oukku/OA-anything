package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_config")
public class AiConfigEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("provider")
    private String provider;
    @TableField("base_url")
    private String baseUrl;
    @TableField("model")
    private String model;
    @TableField("api_key")
    private String apiKey;
    @TableField("temperature")
    private BigDecimal temperature;
    @TableField("max_tokens")
    private Integer maxTokens;
    @TableField("top_k")
    private Integer topK;
    @TableField("mock_mode")
    private Integer mockMode;
    @TableField("is_default")
    private Integer isDefault;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
