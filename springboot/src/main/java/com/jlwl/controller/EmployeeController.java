package com.jlwl.controller;

import com.jlwl.common.R;
import com.jlwl.entity.EmployeeEntity;
import com.jlwl.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/page")
    public R<Map<String, Object>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long deptId
    ) {
        String kw = name != null ? name : keyword;
        var p = employeeService.page(page, limit, kw, deptId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", p.getRecords());
        data.put("total", p.getTotal());
        data.put("size", p.getSize());
        data.put("current", p.getCurrent());
        return R.ok(data);
    }

    @GetMapping("/info/{id}")
    public R<EmployeeEntity> info(@PathVariable Long id) {
        return R.ok(employeeService.get(id));
    }

    @PostMapping("/save")
    public R<Boolean> save(@RequestBody EmployeeEntity e) {
        return R.ok(employeeService.save(e));
    }

    @PostMapping("/update")
    public R<Boolean> update(@RequestBody EmployeeEntity e) {
        return R.ok(employeeService.save(e));
    }

    @DeleteMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(employeeService.delete(id));
    }

    @DeleteMapping("/delete")
    public R<Boolean> deleteBatch(@RequestParam String ids) {
        boolean ok = true;
        for (String s : ids.split(",")) {
            ok = employeeService.delete(Long.parseLong(s.trim())) && ok;
        }
        return R.ok(ok);
    }
}
