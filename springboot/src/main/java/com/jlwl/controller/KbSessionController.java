package com.jlwl.controller;

import com.jlwl.common.R;
import com.jlwl.entity.KbMessageEntity;
import com.jlwl.entity.KbSessionEntity;
import com.jlwl.service.KbMessageService;
import com.jlwl.service.KbSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/session")
@RequiredArgsConstructor
public class KbSessionController {

    private final KbSessionService kbSessionService;
    private final KbMessageService kbMessageService;

    @GetMapping("/list")
    public R<List<KbSessionEntity>> list(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("currentUserId");
        return R.ok(kbSessionService.listByUserId(userId));
    }

    @GetMapping("/messages/{sessionId}")
    public R<List<KbMessageEntity>> messages(@PathVariable Long sessionId) {
        return R.ok(kbMessageService.listBySessionId(sessionId));
    }

    @PostMapping("/save")
    public R<Boolean> save(@RequestBody KbSessionEntity e, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("currentUserId");
        if (e.getUserId() == null) e.setUserId(userId);
        return R.ok(kbSessionService.save(e));
    }

    @PostMapping("/updateTitle")
    public R<Boolean> updateTitle(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String title = (String) body.get("title");
        return R.ok(kbSessionService.updateTitle(id, title));
    }

    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(kbSessionService.delete(id));
    }
}
