package com.jlwl.rag.controller;

import com.jlwl.rag.client.RagClient;
import com.jlwl.rag.dto.RagDocumentInfo;
import com.jlwl.rag.dto.RagQueryRequest;
import com.jlwl.rag.dto.RagQueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * RAG 集成 Controller - 前端通过此 Controller 间接调用 rag-service。
 * 路径前缀 /springboot-oa-v2/rag
 */
@RestController
@RequestMapping("/rag")
public class RagController {

    @Autowired
    private RagClient ragClient;

    /** 健康检查。 */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return ragClient.health();
    }

    /** 知识库问答。 */
    @PostMapping("/query")
    public RagQueryResponse query(@RequestBody RagQueryRequest req) {
        return ragClient.query(req);
    }

    /** 上传文档到知识库。 */
    @PostMapping("/upload")
    public RagDocumentInfo upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("文件为空");
        return ragClient.uploadDocument(file.getOriginalFilename(), file.getBytes());
    }

    /** 列出已索引文档。 */
    @GetMapping("/documents")
    public List<RagDocumentInfo> list() {
        return ragClient.listDocuments();
    }

    /** 删除文档。 */
    @DeleteMapping("/documents/{id}")
    public void delete(@PathVariable("id") String id) {
        ragClient.deleteDocument(id);
    }
}
