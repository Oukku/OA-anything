package com.jlwl.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jlwl.common.R;
import com.jlwl.entity.EmployeeEntity;
import com.jlwl.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/page")
    public R<Map<String, Object>> page(
        @RequestParam(defaultValue = "1") int current,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long deptId
    ) {
        IPage<EmployeeEntity> p = employeeService.page(current, size, keyword, deptId);
        return R.ok(Map.of(
            "records", p.getRecords(),
            "total", p.getTotal(),
            "size", p.getSize(),
            "current", p.getCurrent()
        ));
    }

    @GetMapping("/info/{id}")
    public R<EmployeeEntity> info(@PathVariable Long id) {
        return R.ok(employeeService.get(id));
    }

    @PostMapping("/save")
    public R<Boolean> save(@RequestBody EmployeeEntity e) {
        return R.ok(employeeService.save(e));
    }

    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(employeeService.delete(id));
    }
}
