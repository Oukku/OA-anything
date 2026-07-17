package com.jlwl.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.jlwl.dao.AiConfigDao;
import com.jlwl.entity.AiConfigEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

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
}
