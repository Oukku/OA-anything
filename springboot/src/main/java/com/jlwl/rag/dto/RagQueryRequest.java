package com.jlwl.rag.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 通用 RAG 查询请求。
 * <p>
 * 序列化为 snake_case 发送给 RAG 服务（kb_id、doc_ids、top_k 等）。
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RagQueryRequest {
    private String question;
    /** hybrid | local | global | naive */
    private String mode = "hybrid";
    private String kbId = "default";
    private List<String> docIds;
    private Boolean vlmEnhanced;
    private Integer topK = 10;
    private Double temperature = 0.7;
    private Integer maxTokens = 2048;
    private Long sessionId;
    /** 系统提示词 - 约束 LLM 输出格式，空则用 RAG 引擎默认 */
    private String systemPrompt;
    /** 多模态内容 - 可选 */
    private List<Map<String, Object>> multimodalContent;
}
