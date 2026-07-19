package com.jlwl.rag.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jlwl.rag.config.RagServiceProperties;
import com.jlwl.rag.dto.RagDocumentInfo;
import com.jlwl.rag.dto.RagQueryRequest;
import com.jlwl.rag.dto.RagQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * RAG 服务 HTTP 客户端。
 * <p>
 * 封装对 rag-service 的调用。失败时返回兜底响应，不抛异常给业务层。
 */
@Slf4j
@Component
public class RagClient {

    private final RestTemplate restTemplate;
    private final RagServiceProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public RagClient(RagServiceProperties props) {
        this.props = props;
        ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        ((SimpleClientHttpRequestFactory) factory).setConnectTimeout(5_000);
        ((SimpleClientHttpRequestFactory) factory).setReadTimeout(props.getTimeoutMs());
        this.restTemplate = new RestTemplate(factory);
    }

    /** 健康检查。 */
    public Map<String, Object> health() {
        try {
            ResponseEntity<Map> r = restTemplate.getForEntity(
                props.getServiceUrl() + "/health", Map.class);
            return r.getBody();
        } catch (Exception e) {
            log.warn("RAG health check failed: {}", e.getMessage());
            return Map.of("status", "down", "error", e.getMessage());
        }
    }

    /** 上传文档。 */
    public RagDocumentInfo uploadDocument(String filename, byte[] content) {
        return uploadDocument(filename, content, "default");
    }

    /** 上传文档到指定知识库。 */
    public RagDocumentInfo uploadDocument(String filename, byte[] content, String kbId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("kb_id", kbId);

        HttpEntity<MultiValueMap<String, Object>> req = new HttpEntity<>(body, headers);
        try {
            return restTemplate.postForObject(
                props.getServiceUrl() + "/api/v1/documents/upload",
                req, RagDocumentInfo.class);
        } catch (Exception e) {
            log.error("RAG upload failed: {}", e.getMessage());
            throw new RuntimeException("RAG 上传失败: " + e.getMessage());
        }
    }

    /** 文本/VLM 增强查询。 */
    public RagQueryResponse query(RagQueryRequest req) {
        try {
            return restTemplate.postForObject(
                props.getServiceUrl() + "/api/v1/query",
                req, RagQueryResponse.class);
        } catch (Exception e) {
            log.error("RAG query failed: {}", e.getMessage());
            RagQueryResponse fallback = new RagQueryResponse();
            fallback.setAnswer("RAG 服务暂不可用：" + e.getMessage());
            fallback.setMode(req.getMode());
            return fallback;
        }
    }

    /** 列出已索引文档。 */
    public List<RagDocumentInfo> listDocuments() {
        return listDocuments(null);
    }

    /** 列出指定知识库已索引文档。 */
    public List<RagDocumentInfo> listDocuments(String kbId) {
        try {
            String url = props.getServiceUrl() + "/api/v1/documents";
            if (kbId != null && !kbId.isEmpty()) {
                url += "?kb_id=" + kbId;
            }
            ResponseEntity<Map> r = restTemplate.getForEntity(url, Map.class);
            if (r.getBody() == null) return List.of();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) r.getBody().get("items");
            return items.stream().map(m -> objectMapper.convertValue(m, RagDocumentInfo.class)).toList();
        } catch (Exception e) {
            log.warn("RAG listDocuments failed: {}", e.getMessage());
            return List.of();
        }
    }

    /** 删除文档。 */
    public void deleteDocument(String docId) {
        try {
            restTemplate.delete(props.getServiceUrl() + "/api/v1/documents/" + docId);
        } catch (Exception e) {
            log.warn("RAG delete failed: {}", e.getMessage());
        }
    }

    /** 获取文档知识图谱。 */
    public Map<String, Object> getGraph(String docId) {
        try {
            ResponseEntity<Map> r = restTemplate.getForEntity(
                props.getServiceUrl() + "/api/v1/graph/" + docId, Map.class);
            return r.getBody() != null ? r.getBody() : Map.of();
        } catch (Exception e) {
            log.warn("RAG getGraph failed: {}", e.getMessage());
            return Map.of();
        }
    }

    /** 推送 AI 配置到 RAG 引擎（配置变更后调用）。 */
    public void pushConfig(Map<String, Object> config) {
        try {
            restTemplate.postForEntity(
                props.getServiceUrl() + "/api/v1/config", config, Map.class);
            log.info("RAG 配置已推送: llm={}, embedding={}",
                config.get("llm_model"), config.get("embedding_model"));
        } catch (Exception e) {
            log.warn("RAG pushConfig failed: {}", e.getMessage());
        }
    }
}
