package com.jlwl.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.jlwl.dao.AiConfigDao;
import com.jlwl.entity.AiConfigEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 配置服务 - 三组模型（embedding/llm/reranker）的配置管理 + 连通性测试。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiConfigService {

    private final AiConfigDao aiConfigDao;
    private final com.jlwl.rag.client.RagClient ragClient;

    @Value("${oa.aes.key:oa-v2-aes-key-2026}")
    private String aesKey;

    /**
     * 默认系统提示词 - 约束 Qwen2.5-7B 等小模型输出格式。
     * 必须包含 {context_data} 占位符（hybrid/local/global 模式）或 {content_data}（naive 模式）。
     * RAG 引擎会自动按 mode 转换占位符，无需同时写两个。
     * 可选包含 {response_type} 和 {user_prompt}。
     */
    public static final String DEFAULT_SYSTEM_PROMPT =
        "你是 OA 系统的知识库问答助手。你的任务是基于下方 Context 中提供的文档内容，回答用户的提问。\n\n" +
        "请严格遵循以下规则：\n" +
        "1. 输出语言：始终使用简体中文回答。\n" +
        "2. 输出格式：使用规范的 Markdown，可用标题（#/##/###）、有序列表（1. 2.）、无序列表（-）、表格等。\n" +
        "3. 数字与金额：所有数字、年限、金额、天数必须使用阿拉伯数字（如 5 天、500 元、3 年、1.5 倍）。禁止用星号（*）、字母（a/o/x）或其他符号代替数字。\n" +
        "4. 章节编号：保持原文档章节顺序，如\"第一章\"、\"1.1\"、\"2.3\"。禁止写成\"第章第二\"这种错乱格式。\n" +
        "5. 内容准确性：严格基于 Context 中的内容回答，不要编造信息。Context 中提到的所有数字、年限、金额必须原样引用，不得修改。\n" +
        "6. 引用来源：不要在末尾添加 References 列表，除非用户明确要求。\n" +
        "7. 简洁性：回答简明扼要，避免重复。\n" +
        "8. 回答要求：必须直接回答用户问题。如果 Context 中包含相关内容，必须给出具体回答；只有当 Context 完全没有相关内容时，才能说明\"知识库中暂无相关内容\"。\n\n" +
        "---Context---\n{context_data}";

    private AES aes() {
        String key = SecureUtil.md5(aesKey).substring(0, 16);
        return SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
    }

    public String encryptApiKey(String plain) {
        if (plain == null || plain.isEmpty()) return null;
        return Base64.getEncoder().encodeToString(aes().encrypt(plain));
    }

    public String decryptApiKey(String cipher) {
        if (cipher == null || cipher.isEmpty()) return null;
        return aes().decryptStr(Base64.getDecoder().decode(cipher));
    }

    public AiConfigEntity getDefault() {
        AiConfigEntity cfg = aiConfigDao.getDefault();
        if (cfg == null) {
            cfg = new AiConfigEntity();
            cfg.setMockMode(1);
            cfg.setTopK(5);
            cfg.setEmbeddingProvider("siliconflow");
            cfg.setEmbeddingBaseUrl("https://api.siliconflow.cn/v1");
            cfg.setEmbeddingModel("BAAI/bge-m3");
            cfg.setEmbeddingDim(1024);
            cfg.setLlmProvider("siliconflow");
            cfg.setLlmBaseUrl("https://api.siliconflow.cn/v1");
            cfg.setLlmModel("Qwen/Qwen2.5-7B-Instruct");
            cfg.setLlmTemperature(new java.math.BigDecimal("0.7"));
            cfg.setLlmMaxTokens(2048);
            cfg.setRerankerProvider("siliconflow");
            cfg.setRerankerBaseUrl("https://api.siliconflow.cn/v1");
            cfg.setRerankerModel("BAAI/bge-reranker-v2-m3");
        }
        // system_prompt 为空时回填默认值
        if (cfg.getSystemPrompt() == null || cfg.getSystemPrompt().isEmpty()) {
            cfg.setSystemPrompt(DEFAULT_SYSTEM_PROMPT);
        }
        return cfg;
    }

    /** 返回给前端时脱敏 api_key。 */
    public AiConfigEntity getDefaultForDisplay() {
        AiConfigEntity cfg = getDefault();
        if (cfg.getEmbeddingApiKey() != null) cfg.setEmbeddingApiKey("");
        if (cfg.getLlmApiKey() != null) cfg.setLlmApiKey("");
        if (cfg.getRerankerApiKey() != null) cfg.setRerankerApiKey("");
        return cfg;
    }

    public boolean save(AiConfigEntity e) {
        e.setUpdateTime(LocalDateTime.now());
        if (e.getMockMode() == null) e.setMockMode(0);
        if (e.getIsDefault() == null) e.setIsDefault(1);

        // 三组 api_key 分别处理：空值保留旧值，非空加密
        if (e.getId() != null) {
            AiConfigEntity old = aiConfigDao.selectById(e.getId());
            if (old != null) {
                e.setEmbeddingApiKey(mergeKey(e.getEmbeddingApiKey(), old.getEmbeddingApiKey()));
                e.setLlmApiKey(mergeKey(e.getLlmApiKey(), old.getLlmApiKey()));
                e.setRerankerApiKey(mergeKey(e.getRerankerApiKey(), old.getRerankerApiKey()));
            }
        } else {
            e.setEmbeddingApiKey(encryptIfPresent(e.getEmbeddingApiKey()));
            e.setLlmApiKey(encryptIfPresent(e.getLlmApiKey()));
            e.setRerankerApiKey(encryptIfPresent(e.getRerankerApiKey()));
            e.setCreateTime(LocalDateTime.now());
        }

        return e.getId() == null ? aiConfigDao.insert(e) > 0 : aiConfigDao.updateById(e) > 0;
    }

    /** 保存配置并推送到 RAG 引擎。 */
    public boolean saveAndPush(AiConfigEntity e) {
        boolean ok = save(e);
        if (ok) {
            try {
                AiConfigEntity decrypted = getForRag();
                Map<String, Object> cfg = new HashMap<>();
                cfg.put("embedding_provider", decrypted.getEmbeddingProvider());
                cfg.put("embedding_base_url", decrypted.getEmbeddingBaseUrl());
                cfg.put("embedding_model", decrypted.getEmbeddingModel());
                cfg.put("embedding_api_key", decrypted.getEmbeddingApiKey());
                cfg.put("embedding_dim", decrypted.getEmbeddingDim());
                cfg.put("llm_provider", decrypted.getLlmProvider());
                cfg.put("llm_base_url", decrypted.getLlmBaseUrl());
                cfg.put("llm_model", decrypted.getLlmModel());
                cfg.put("llm_api_key", decrypted.getLlmApiKey());
                cfg.put("llm_temperature", decrypted.getLlmTemperature());
                cfg.put("llm_max_tokens", decrypted.getLlmMaxTokens());
                cfg.put("system_prompt", decrypted.getSystemPrompt() != null
                    ? decrypted.getSystemPrompt() : DEFAULT_SYSTEM_PROMPT);
                cfg.put("reranker_provider", decrypted.getRerankerProvider());
                cfg.put("reranker_base_url", decrypted.getRerankerBaseUrl());
                cfg.put("reranker_model", decrypted.getRerankerModel());
                cfg.put("reranker_api_key", decrypted.getRerankerApiKey());
                cfg.put("mock_mode", decrypted.getMockMode());
                ragClient.pushConfig(cfg);
            } catch (Exception ex) {
                log.warn("推送配置到 RAG 引擎失败: {}", ex.getMessage());
            }
        }
        return ok;
    }

    private String mergeKey(String newKey, String oldEncrypted) {
        if (newKey == null || newKey.isEmpty()) return oldEncrypted;
        return encryptApiKey(newKey);
    }

    private String encryptIfPresent(String plain) {
        return (plain != null && !plain.isEmpty()) ? encryptApiKey(plain) : null;
    }

    /** RAG 引擎使用 - 解密后的完整配置。 */
    public AiConfigEntity getForRag() {
        AiConfigEntity cfg = getDefault();
        cfg.setEmbeddingApiKey(decryptApiKey(cfg.getEmbeddingApiKey()));
        cfg.setLlmApiKey(decryptApiKey(cfg.getLlmApiKey()));
        cfg.setRerankerApiKey(decryptApiKey(cfg.getRerankerApiKey()));
        return cfg;
    }

    // ===== 连通性测试 =====

    /** 测试 Embedding 模型。 */
    public Map<String, Object> testEmbedding(AiConfigEntity e) {
        return testModel("embedding", e.getEmbeddingProvider(), e.getEmbeddingBaseUrl(),
            e.getEmbeddingModel(), e.getEmbeddingApiKey(), e.getId(),
            e.getMockMode(), "embeddings", true);
    }

    /** 测试 LLM 模型。 */
    public Map<String, Object> testLlm(AiConfigEntity e) {
        return testModel("llm", e.getLlmProvider(), e.getLlmBaseUrl(),
            e.getLlmModel(), e.getLlmApiKey(), e.getId(),
            e.getMockMode(), "chat/completions", false);
    }

    /** 测试 Reranker 模型。 */
    public Map<String, Object> testReranker(AiConfigEntity e) {
        return testModel("reranker", e.getRerankerProvider(), e.getRerankerBaseUrl(),
            e.getRerankerModel(), e.getRerankerApiKey(), e.getId(),
            e.getMockMode(), "rerank", false);
    }

    private Map<String, Object> testModel(String type, String provider, String baseUrl,
                                          String model, String apiKey, Long configId,
                                          Integer mockMode, String endpoint, boolean isEmbedding) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", type);
        result.put("provider", provider);
        result.put("model", model);

        // 测试时优先使用请求体的 mockMode，未传则回退到 DB
        Integer effectiveMock = mockMode;
        if (effectiveMock == null) {
            effectiveMock = getDefault().getMockMode();
        }
        if (Integer.valueOf(1).equals(effectiveMock)) {
            result.put("ok", true);
            result.put("msg", "MOCK 模式跳过测试");
            return result;
        }

        String key = resolveApiKey(apiKey, configId, type);
        if (key == null || key.isEmpty()) {
            result.put("ok", false);
            result.put("msg", "API Key 不能为空");
            return result;
        }

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(8000);
            factory.setReadTimeout(15000);
            RestTemplate rest = new RestTemplate(factory);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + key);
            headers.set("Content-Type", "application/json");

            String url = normalizeUrl(baseUrl) + "/" + endpoint;
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);

            if (isEmbedding) {
                body.put("input", "hello");
                ResponseEntity<Map> rsp = rest.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
                result.put("ok", rsp.getStatusCode().is2xxSuccessful());
                result.put("msg", "Embedding 接口连接正常");
            } else if ("rerank".equals(endpoint)) {
                // 硅基流动 rerank 接口
                body.put("query", "测试");
                body.put("documents", List.of("测试文档"));
                ResponseEntity<Map> rsp = rest.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
                result.put("ok", rsp.getStatusCode().is2xxSuccessful());
                result.put("msg", "Reranker 接口连接正常");
            } else {
                body.put("messages", List.of(Map.of("role", "user", "content", "hi")));
                body.put("max_tokens", 5);
                body.put("stream", false);
                ResponseEntity<Map> rsp = rest.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
                result.put("ok", rsp.getStatusCode().is2xxSuccessful());
                result.put("msg", "LLM 接口连接正常");
            }
        } catch (Exception ex) {
            result.put("ok", false);
            String msg = ex.getMessage();
            if (msg == null || msg.isEmpty()) msg = "连接失败，请检查配置";
            result.put("msg", msg);
        }
        return result;
    }

    private String normalizeUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) return "https://api.siliconflow.cn/v1";
        String url = baseUrl.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://" + url;
        return url.replaceAll("/$", "");
    }

    private String resolveApiKey(String inputKey, Long configId, String type) {
        if (inputKey != null && !inputKey.isEmpty()) return inputKey;
        if (configId != null) {
            AiConfigEntity old = aiConfigDao.selectById(configId);
            if (old != null) {
                String enc = switch (type) {
                    case "embedding" -> old.getEmbeddingApiKey();
                    case "llm" -> old.getLlmApiKey();
                    case "reranker" -> old.getRerankerApiKey();
                    default -> null;
                };
                return decryptApiKey(enc);
            }
        }
        return null;
    }
}
