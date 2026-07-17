package com.jlwl.controller;

import com.jlwl.common.R;
import com.jlwl.entity.AiConfigEntity;
import com.jlwl.service.AiConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai/config")
@RequiredArgsConstructor
public class AiConfigController {

    private final AiConfigService aiConfigService;

    @GetMapping("/info")
    public R<AiConfigEntity> info() {
        AiConfigEntity cfg = aiConfigService.getDefault();
        cfg.setApiKey(null);
        return R.ok(cfg);
    }

    @PostMapping("/save")
    public R<Boolean> save(@RequestBody AiConfigEntity e) {
        return R.ok(aiConfigService.save(e));
    }

    @PostMapping("/test")
    public R<Map<String, Object>> test(@RequestBody AiConfigEntity e) {
        return R.ok(aiConfigService.testConnection(e));
    }
}
