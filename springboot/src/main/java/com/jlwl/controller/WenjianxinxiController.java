package com.jlwl.controller;

import com.jlwl.common.R;
import com.jlwl.entity.WenjianxinxiEntity;
import com.jlwl.service.WenjianxinxiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/wenjianxinxi")
@RequiredArgsConstructor
public class WenjianxinxiController {

    private final WenjianxinxiService service;

    @GetMapping("/page")
    public R<Map<String, Object>> page(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String sfsh
    ) {
        String kw = name != null ? name : keyword;
        var p = service.page(page, limit, kw, null, sfsh);
        Map<String, Object> data = new HashMap<>();
        data.put("list", p.getRecords());
        data.put("total", p.getTotal());
        data.put("size", p.getSize());
        data.put("current", p.getCurrent());
        return R.ok(data);
    }

    @GetMapping("/info/{id}")
    public R<WenjianxinxiEntity> info(@PathVariable Long id) {
        return R.ok(service.get(id));
    }

    @PostMapping("/save")
    public R<Boolean> save(@RequestBody WenjianxinxiEntity e) {
        if (e.getSfsh() == null) e.setSfsh("否");
        return R.ok(service.save(e));
    }

    @PostMapping("/update")
    public R<Boolean> update(@RequestBody WenjianxinxiEntity e) {
        return R.ok(service.save(e));
    }

    /**
     * 文件审核接口
     * body: { id, sfsh: 是|驳回, shhf: 审核回复 }
     */
    @PostMapping("/approve")
    public R<Boolean> approve(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String sfsh = (String) body.getOrDefault("sfsh", "是");
        String shhf = (String) body.getOrDefault("shhf", "");
        WenjianxinxiEntity e = service.get(id);
        if (e == null) return R.fail("文件不存在");
        e.setSfsh(sfsh);
        e.setShhf(shhf);
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
