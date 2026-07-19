package com.jlwl.rag.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档元信息。
 * <p>
 * RAG 服务返回 snake_case 字段（size_bytes、kb_id、uploaded_at、page_count 等），
 * 用 @JsonNaming 自动映射到 camelCase 字段。
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RagDocumentInfo {
    private String id;
    private String filename;
    private Long sizeBytes;
    private String status;
    private String kbId;
    private LocalDateTime uploadedAt;
    private Integer pageCount;
    private Integer chunkCount;
    private Integer entityCount;
}
