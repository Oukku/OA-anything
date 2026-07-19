package com.jlwl.controller;

import com.jlwl.common.R;
import com.jlwl.entity.AiConfigEntity;
import com.jlwl.service.AiConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 配置 Controller - 三组模型配置管理 + 连通性测试。
 */
@RestController
@RequestMapping("/ai/config")
@RequiredArgsConstructor
public class AiConfigController {

    private final AiConfigService aiConfigService;

    @GetMapping("/info")
    public R<AiConfigEntity> info() {
        return R.ok(aiConfigService.getDefaultForDisplay());
    }

    @PostMapping("/save")
    public R<Boolean> save(@RequestBody AiConfigEntity e) {
        return R.ok(aiConfigService.saveAndPush(e));
    }

    @PostMapping("/test/embedding")
    public R<Map<String, Object>> testEmbedding(@RequestBody AiConfigEntity e) {
        return R.ok(aiConfigService.testEmbedding(e));
    }

    @PostMapping("/test/llm")
    public R<Map<String, Object>> testLlm(@RequestBody AiConfigEntity e) {
        return R.ok(aiConfigService.testLlm(e));
    }

    @PostMapping("/test/reranker")
    public R<Map<String, Object>> testReranker(@RequestBody AiConfigEntity e) {
        return R.ok(aiConfigService.testReranker(e));
    }
}
