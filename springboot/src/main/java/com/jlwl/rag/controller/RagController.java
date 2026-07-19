package com.jlwl.rag.controller;

import com.jlwl.common.R;
import com.jlwl.entity.AiConfigEntity;
import com.jlwl.entity.KbDocumentEntity;
import com.jlwl.entity.KbMessageEntity;
import com.jlwl.entity.KbSessionEntity;
import com.jlwl.rag.client.RagClient;
import com.jlwl.rag.dto.RagDocumentInfo;
import com.jlwl.rag.dto.RagQueryRequest;
import com.jlwl.rag.dto.RagQueryResponse;
import com.jlwl.service.AiConfigService;
import com.jlwl.service.KbDocumentService;
import com.jlwl.service.KbMessageService;
import com.jlwl.service.KbSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 集成 Controller - 前端通过此 Controller 间接调用 rag-service。
 * 路径前缀 /springboot-oa-v2/rag
 */
@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagClient ragClient;
    private final KbDocumentService kbDocumentService;
    private final KbSessionService kbSessionService;
    private final KbMessageService kbMessageService;
    private final AiConfigService aiConfigService;

    /** 健康检查。 */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return ragClient.health();
    }

    /** 知识库问答（关联会话）。 */
    @PostMapping("/query")
    public R<RagQueryResponse> query(@RequestBody RagQueryRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");

        // 注入生产级配置
        AiConfigEntity cfg = aiConfigService.getForRag();
        if (cfg.getMockMode() != null && cfg.getMockMode() == 1) {
            req.setKbId("default");
        }
        if (req.getTopK() == null) req.setTopK(cfg.getTopK());
        if (cfg.getLlmTemperature() != null) req.setTemperature(cfg.getLlmTemperature().doubleValue());
        if (cfg.getLlmMaxTokens() != null) req.setMaxTokens(cfg.getLlmMaxTokens());

        long t0 = System.currentTimeMillis();
        RagQueryResponse resp = ragClient.query(req);
        resp.setDurationMs((int) (System.currentTimeMillis() - t0));

        // 保存会话消息
        if (req.getSessionId() != null) {
            kbMessageService.save(buildMsg(req.getSessionId(), "user", req.getQuestion(), req.getMode(), 0));
            kbMessageService.save(buildMsg(req.getSessionId(), "bot", resp.getAnswer(), resp.getMode(), resp.getDurationMs()));
            kbSessionService.updateTitle(req.getSessionId(), truncateTitle(req.getQuestion()));
        }

        return R.ok(resp);
    }

    /** 上传文档到知识库。 */
    @PostMapping("/upload")
    public R<RagDocumentInfo> upload(
        @RequestParam("file") MultipartFile file,
        @RequestParam("kbId") Long kbId,
        HttpServletRequest request
    ) throws Exception {
        if (file.isEmpty()) return R.fail("文件为空");
        Long userId = (Long) request.getAttribute("currentUserId");

        KbDocumentEntity doc = new KbDocumentEntity();
        doc.setKbId(kbId);
        doc.setFilename(file.getOriginalFilename());
        doc.setFileSize(file.getSize());
        doc.setFileType(ext(file.getOriginalFilename()));
        doc.setStatus(0); // 处理中（RAG 异步解析）
        kbDocumentService.save(doc, userId);

        try {
            RagDocumentInfo info = ragClient.uploadDocument(
                file.getOriginalFilename(), file.getBytes(), String.valueOf(kbId));
            doc.setRagDocId(info.getId());
            doc.setStatus(0); // RAG 异步解析中，保持 0
            kbDocumentService.save(doc, userId);
            return R.ok(info);
        } catch (Exception e) {
            doc.setStatus(-1);
            doc.setErrorMsg(e.getMessage());
            kbDocumentService.save(doc, userId);
            return R.fail("RAG 上传失败: " + e.getMessage());
        }
    }

    /** 列出已索引文档（按知识库）- 合并 RAG 服务实时状态。 */
    @GetMapping("/documents")
    public R<List<Map<String, Object>>> list(@RequestParam Long kbId) {
        List<KbDocumentEntity> docs = kbDocumentService.listByKbId(kbId);
        List<RagDocumentInfo> ragDocs = ragClient.listDocuments(String.valueOf(kbId));
        java.util.Map<String, RagDocumentInfo> ragMap = ragDocs.stream()
            .collect(Collectors.toMap(RagDocumentInfo::getId, r -> r, (a, b) -> a));

        List<Map<String, Object>> result = docs.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("kbId", d.getKbId());
            m.put("filename", d.getFilename());
            m.put("fileSize", d.getFileSize());
            m.put("fileType", d.getFileType());
            m.put("ragDocId", d.getRagDocId());
            m.put("createTime", d.getCreateTime());
            m.put("errorMsg", d.getErrorMsg());
            m.put("dbStatus", d.getStatus());
            if (d.getRagDocId() != null && ragMap.containsKey(d.getRagDocId())) {
                RagDocumentInfo r = ragMap.get(d.getRagDocId());
                m.put("ragStatus", r.getStatus()); // parsing/indexed/failed
                m.put("pageCount", r.getPageCount());
                m.put("chunkCount", r.getChunkCount());
                // 用 RAG 实时状态覆盖 DB 状态
                m.put("status", mapRagStatus(r.getStatus(), d.getStatus()));
            } else {
                m.put("status", d.getStatus());
            }
            return m;
        }).collect(Collectors.toList());
        return R.ok(result);
    }

    /** RAG 字符串状态映射到 DB Integer 状态：parsing->0, indexed->1, failed->-1。 */
    private Integer mapRagStatus(String ragStatus, Integer dbStatus) {
        if (ragStatus == null) return dbStatus;
        return switch (ragStatus) {
            case "indexed" -> 1;
            case "failed" -> -1;
            case "parsing", "queued" -> 0;
            default -> dbStatus;
        };
    }

    /** 文档预览（解析后文本 + 分块）。 */
    @GetMapping("/preview/{docId}")
    public R<Map<String, Object>> preview(@PathVariable String docId) {
        return R.ok(ragClient.previewDocument(docId));
    }

    /** 重新解析文档。 */
    @PostMapping("/reparse/{docId}")
    public R<Map<String, Object>> reparse(@PathVariable String docId) {
        return R.ok(ragClient.reparseDocument(docId));
    }

    /** 删除文档。 */
    @DeleteMapping("/documents/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        KbDocumentEntity doc = kbDocumentService.get(id);
        if (doc != null && doc.getRagDocId() != null) {
            ragClient.deleteDocument(doc.getRagDocId());
        }
        return R.ok(kbDocumentService.delete(id));
    }

    /** 文档知识图谱。 */
    @GetMapping("/graph/{docId}")
    public R<Map<String, Object>> graph(@PathVariable String docId) {
        return R.ok(ragClient.getGraph(docId));
    }

    /** 查询 RAG 统计。 */
    @GetMapping("/stats/{kbId}")
    public R<Map<String, Object>> stats(@PathVariable String kbId) {
        Map<String, Object> data = new HashMap<>();
        data.put("kbId", kbId);
        data.put("documents", kbDocumentService.listByKbId(Long.valueOf(kbId)).size());
        return R.ok(data);
    }

    private KbMessageEntity buildMsg(Long sessionId, String role, String content, String mode, int durationMs) {
        KbMessageEntity m = new KbMessageEntity();
        m.setSessionId(sessionId);
        m.setRole(role);
        m.setContent(content);
        m.setMode(mode);
        m.setDurationMs(durationMs);
        m.setCreateTime(LocalDateTime.now());
        return m;
    }

    private String truncateTitle(String question) {
        if (question == null) return "新会话";
        return question.length() > 20 ? question.substring(0, 20) + "..." : question;
    }

    private String ext(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx + 1) : "";
    }
}
