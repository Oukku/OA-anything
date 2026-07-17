package com.jlwl.controller;

import com.jlwl.common.R;
import com.jlwl.entity.GonggaoxinxiEntity;
import com.jlwl.service.GonggaoxinxiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gonggaoxinxi")
@RequiredArgsConstructor
public class GonggaoxinxiController {

    private final GonggaoxinxiService service;

    @GetMapping("/page")
    public R<Map<String, Object>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status
    ) {
        String kw = title != null ? title : keyword;
        var p = service.page(page, limit, kw, status);
        Map<String, Object> data = new HashMap<>();
        data.put("list", p.getRecords());
        data.put("total", p.getTotal());
        data.put("size", p.getSize());
        data.put("current", p.getCurrent());
        return R.ok(data);
    }

    @GetMapping("/info/{id}")
    public R<GonggaoxinxiEntity> info(@PathVariable Long id) {
        return R.ok(service.get(id));
    }

    @PostMapping("/save")
    public R<Boolean> save(@RequestBody GonggaoxinxiEntity e) {
        return R.ok(service.save(e));
    }

    @PostMapping("/update")
    public R<Boolean> update(@RequestBody GonggaoxinxiEntity e) {
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
