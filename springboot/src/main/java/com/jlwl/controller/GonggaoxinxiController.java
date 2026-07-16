package com.jlwl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jlwl.common.R;
import com.jlwl.entity.GonggaoxinxiEntity;
import com.jlwl.service.GonggaoxinxiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/gonggaoxinxi")
@RequiredArgsConstructor
public class GonggaoxinxiController {

    private final GonggaoxinxiService service;

    @GetMapping("/page")
    public R<Map<String, Object>> page(
        @RequestParam(defaultValue = "1") int current,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status
    ) {
        IPage<GonggaoxinxiEntity> p = service.page(current, size, keyword, status);
        return R.ok(Map.of(
            "records", p.getRecords(),
            "total", p.getTotal(),
            "size", p.getSize(),
            "current", p.getCurrent()
        ));
    }

    @GetMapping("/info/{id}")
    public R<GonggaoxinxiEntity> info(@PathVariable Long id) {
        return R.ok(service.get(id));
    }

    @PostMapping("/save")
    public R<Boolean> save(@RequestBody GonggaoxinxiEntity e) {
        return R.ok(service.save(e));
    }

    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(service.delete(id));
    }
}
