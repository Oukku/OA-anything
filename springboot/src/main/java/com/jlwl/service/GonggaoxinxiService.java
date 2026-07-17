package com.jlwl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jlwl.dao.GonggaoxinxiDao;
import com.jlwl.entity.GonggaoxinxiEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GonggaoxinxiService {

    private final GonggaoxinxiDao dao;

    public IPage<GonggaoxinxiEntity> page(int current, int size, String keyword, Integer status) {
        QueryWrapper<GonggaoxinxiEntity> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like("biaoti", keyword).or().like("gonggaoneirong", keyword));
        }
        qw.orderByDesc("addtime");
        return dao.selectPage(new Page<>(current, size), qw);
    }

    public GonggaoxinxiEntity get(Long id) {
        return dao.selectById(id);
    }

    public boolean save(GonggaoxinxiEntity e) {
        if (e.getFabuTime() == null) e.setFabuTime(LocalDateTime.now());
        if (e.getId() == null) return dao.insert(e) > 0;
        return dao.updateById(e) > 0;
    }

    public boolean delete(Long id) { return dao.deleteById(id) > 0; }
}
