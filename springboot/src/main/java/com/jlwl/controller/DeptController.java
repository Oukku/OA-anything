package com.jlwl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jlwl.common.R;
import com.jlwl.entity.DeptEntity;
import com.jlwl.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/list")
    public R<List<DeptEntity>> list() {
        return R.ok(deptService.list());
    }

    @GetMapping("/page")
    public R<Map<String, Object>> page(
        @RequestParam(defaultValue = "1") int current,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword
    ) {
        IPage<DeptEntity> p = deptService.page(current, size, keyword);
        Map<String, Object> data = Map.of(
            "records", p.getRecords(),
            "total", p.getTotal(),
            "size", p.getSize(),
            "current", p.getCurrent()
        );
        return R.ok(data);
    }

    @PostMapping("/save")
    public R<Boolean> save(@RequestBody DeptEntity dept) {
        return R.ok(deptService.save(dept));
    }

    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(deptService.delete(id));
    }
}
