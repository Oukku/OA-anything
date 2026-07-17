package com.jlwl.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.jlwl.dao.AiConfigDao;
import com.jlwl.entity.AiConfigEntity;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AiConfigService {

    private final AiConfigDao aiConfigDao;

    @Value("${oa.aes.key:oa-v2-aes-key-2026}")
    private String aesKey;

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
            cfg.setProvider("mock");
            cfg.setBaseUrl("");
            cfg.setModel("mock-llm");
            cfg.setMockMode(1);
            cfg.setTopK(5);
        }
        return cfg;
    }

    public boolean save(AiConfigEntity e) {
        e.setUpdateTime(LocalDateTime.now());
        if (e.getMockMode() == null) e.setMockMode(0);
        if (e.getIsDefault() == null) e.setIsDefault(1);
        if (e.getApiKey() != null && !e.getApiKey().isEmpty()) {
            e.setApiKey(encryptApiKey(e.getApiKey()));
        }
        if (e.getId() == null) {
            e.setCreateTime(LocalDateTime.now());
            return aiConfigDao.insert(e) > 0;
        }
        if (e.getApiKey() == null || e.getApiKey().isEmpty()) {
            AiConfigEntity old = aiConfigDao.selectById(e.getId());
            if (old != null) e.setApiKey(old.getApiKey());
        }
        return aiConfigDao.updateById(e) > 0;
    }

    public AiConfigEntity getForRag() {
        AiConfigEntity cfg = getDefault();
        cfg.setApiKey(decryptApiKey(cfg.getApiKey()));
        return cfg;
    }

    /**
     * 真实测试 AI 接口连通性。
     * @param e 前端传入的配置（可能未包含已加密的 apiKey，需要从数据库补全）
     */
    public Map<String, Object> testConnection(AiConfigEntity e) {
        Map<String, Object> result = new HashMap<>();
        result.put("provider", e.getProvider());
        result.put("model", e.getModel());
        result.put("mock", e.getMockMode());

        if (Integer.valueOf(1).equals(e.getMockMode()) || "mock".equalsIgnoreCase(e.getProvider())) {
            result.put("ok", true);
            result.put("msg", "MOCK 模式无需连通性测试");
            return result;
        }

        String baseUrl = normalizeBaseUrl(e.getBaseUrl());
        String apiKey = resolveApiKey(e);
        if (apiKey == null || apiKey.isEmpty()) {
            result.put("ok", false);
            result.put("msg", "API Key 不能为空");
            return result;
        }

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(8000);
            RestTemplate rest = new RestTemplate(factory);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            String provider = e.getProvider() == null ? "" : e.getProvider().toLowerCase();
            if ("ollama".equals(provider)) {
                // Ollama 列出本地模型
                String url = baseUrl.replaceAll("/$", "") + "/api/tags";
                ResponseEntity<Map> rsp = rest.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
                result.put("ok", rsp.getStatusCode().is2xxSuccessful());
                result.put("msg", "Ollama 连接正常");
            } else {
                // OpenAI / OpenAI 兼容 / MiniMax：用 chat.completions 发一个极简请求
                String url = baseUrl.replaceAll("/$", "") + "/chat/completions";
                Map<String, Object> body = new HashMap<>();
                body.put("model", e.getModel());
                body.put("messages", List.of(Map.of("role", "user", "content", "hi")));
                body.put("max_tokens", 5);
                body.put("stream", false);
                ResponseEntity<Map> rsp = rest.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
                result.put("ok", rsp.getStatusCode().is2xxSuccessful());
                result.put("msg", "AI 接口连接正常");
            }
        } catch (Exception ex) {
            result.put("ok", false);
            String msg = ex.getMessage();
            if (msg == null || msg.isEmpty()) {
                msg = "连接失败，请检查 Base URL、API Key 与模型名称";
            }
            result.put("msg", msg);
        }
        return result;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return "https://api.openai.com/v1";
        }
        String url = baseUrl.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }

    private String resolveApiKey(AiConfigEntity e) {
        if (e.getApiKey() != null && !e.getApiKey().isEmpty()) {
            return e.getApiKey();
        }
        if (e.getId() != null) {
            AiConfigEntity old = aiConfigDao.selectById(e.getId());
            if (old != null) {
                return decryptApiKey(old.getApiKey());
            }
        }
        return null;
    }
}
