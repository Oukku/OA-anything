package com.jlwl.rag.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * RAG 查询响应。
 */
@Data
public class RagQueryResponse {
    private String answer;
    private String mode;
    private Integer durationMs;
    private List<Map<String, Object>> sources;
}
