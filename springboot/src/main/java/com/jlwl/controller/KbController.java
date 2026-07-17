package com.jlwl.controller;

import com.jlwl.common.R;
import com.jlwl.entity.KbEntity;
import com.jlwl.service.KbService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/kb")
@RequiredArgsConstructor
public class KbController {

    private final KbService kbService;

    @GetMapping("/list")
    public R<List<KbEntity>> list(@RequestParam(required = false, defaultValue = "false") boolean all) {
        return R.ok(all ? kbService.listAll() : kbService.listActive());
    }

    @GetMapping("/info/{id}")
    public R<KbEntity> info(@PathVariable Long id) {
        return R.ok(kbService.get(id));
    }

    @PostMapping("/save")
    public R<Boolean> save(@RequestBody KbEntity e, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("currentUserId");
        return R.ok(kbService.save(e, userId));
    }

    @PostMapping("/update")
    public R<Boolean> update(@RequestBody KbEntity e, HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("currentUserId");
        return R.ok(kbService.save(e, userId));
    }

    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(kbService.delete(id));
    }

    @DeleteMapping("/delete")
    public R<Boolean> deleteBatch(@RequestParam String ids) {
        boolean ok = true;
        for (String s : ids.split(",")) {
            ok = kbService.delete(Long.parseLong(s.trim())) && ok;
        }
        return R.ok(ok);
    }
}
