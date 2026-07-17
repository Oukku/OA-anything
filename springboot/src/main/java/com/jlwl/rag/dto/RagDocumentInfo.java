package com.jlwl.rag.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档元信息。
 */
@Data
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
