package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 配置 - 三组模型配置（embedding / llm / reranker）。
 * 对应 RAG 多阶段流水线：向量检索、重排序、LLM 总结。
 */
@Data
@TableName("ai_config")
public class AiConfigEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    // ===== 通用 =====
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

    // ===== Embedding 模型（向量检索） =====
    @TableField("embedding_provider")
    private String embeddingProvider;
    @TableField("embedding_base_url")
    private String embeddingBaseUrl;
    @TableField("embedding_model")
    private String embeddingModel;
    @TableField("embedding_api_key")
    private String embeddingApiKey;
    @TableField("embedding_dim")
    private Integer embeddingDim;

    // ===== LLM 模型（关键词提取 + 总结回答） =====
    @TableField("llm_provider")
    private String llmProvider;
    @TableField("llm_base_url")
    private String llmBaseUrl;
    @TableField("llm_model")
    private String llmModel;
    @TableField("llm_api_key")
    private String llmApiKey;
    @TableField("llm_temperature")
    private BigDecimal llmTemperature;
    @TableField("llm_max_tokens")
    private Integer llmMaxTokens;

    // ===== Reranker 模型（重排序） =====
    @TableField("reranker_provider")
    private String rerankerProvider;
    @TableField("reranker_base_url")
    private String rerankerBaseUrl;
    @TableField("reranker_model")
    private String rerankerModel;
    @TableField("reranker_api_key")
    private String rerankerApiKey;
}
