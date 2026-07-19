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
        doc.setStatus(0);
        kbDocumentService.save(doc, userId);

        try {
            RagDocumentInfo info = ragClient.uploadDocument(
                file.getOriginalFilename(), file.getBytes(), String.valueOf(kbId));
            doc.setRagDocId(info.getId());
            doc.setStatus(1);
            kbDocumentService.save(doc, userId);
            return R.ok(info);
        } catch (Exception e) {
            doc.setStatus(-1);
            doc.setErrorMsg(e.getMessage());
            kbDocumentService.save(doc, userId);
            return R.fail("RAG 上传失败: " + e.getMessage());
        }
    }

    /** 列出已索引文档（按知识库）。 */
    @GetMapping("/documents")
    public R<List<KbDocumentEntity>> list(@RequestParam Long kbId) {
        return R.ok(kbDocumentService.listByKbId(kbId));
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
