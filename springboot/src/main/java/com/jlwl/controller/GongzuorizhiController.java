package com.jlwl.controller;

import com.jlwl.common.R;
import com.jlwl.entity.GongzuorizhiEntity;
import com.jlwl.service.GongzuorizhiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gongzuorizhi")
@RequiredArgsConstructor
public class GongzuorizhiController {

    private final GongzuorizhiService service;

    @GetMapping("/page")
    public R<Map<String, Object>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String date,
        @RequestParam(required = false) Long userId
    ) {
        String kw = title != null ? title : keyword;
        var p = service.page(page, limit, kw, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", p.getRecords());
        data.put("total", p.getTotal());
        data.put("size", p.getSize());
        data.put("current", p.getCurrent());
        return R.ok(data);
    }

    @GetMapping("/info/{id}")
    public R<GongzuorizhiEntity> info(@PathVariable Long id) {
        return R.ok(service.get(id));
    }

    @PostMapping("/save")
    public R<Boolean> save(@RequestBody GongzuorizhiEntity e) {
        return R.ok(service.save(e));
    }

    @PostMapping("/update")
    public R<Boolean> update(@RequestBody GongzuorizhiEntity e) {
        return R.ok(service.save(e));
    }

    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(service.delete(id));
    }

    @DeleteMapping("/delete")
    public R<Boolean> deleteBatch(@RequestParam String ids) {
        boolean ok = true;
        for (String s : ids.split(",")) {
            ok = service.delete(Long.parseLong(s.trim())) && ok;
        }
        return R.ok(ok);
    }
}
