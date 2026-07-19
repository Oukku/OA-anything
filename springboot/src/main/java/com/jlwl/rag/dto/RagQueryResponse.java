package com.jlwl.rag.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * RAG 查询响应。
 * <p>
 * RAG 服务返回 snake_case 字段（duration_ms 等），
 * 用 @JsonNaming 自动映射到 camelCase 字段。
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RagQueryResponse {
    private String answer;
    private String mode;
    private Integer durationMs;
    private List<Map<String, Object>> sources;
}
