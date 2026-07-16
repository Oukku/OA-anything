package com.jlwl.rag.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 通用 RAG 查询请求。
 */
@Data
public class RagQueryRequest {
    private String question;
    /** hybrid | local | global | naive */
    private String mode = "hybrid";
    private String kbId = "default";
    private Boolean vlmEnhanced;
    private Integer topK = 10;
    /** 多模态内容 - 可选 */
    private List<Map<String, Object>> multimodalContent;
}
