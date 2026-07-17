package com.jlwl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jlwl.dao.GongzuorizhiDao;
import com.jlwl.entity.GongzuorizhiEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GongzuorizhiService {

    private final GongzuorizhiDao dao;

    public IPage<GongzuorizhiEntity> page(int current, int size, String keyword, Long userId) {
        QueryWrapper<GongzuorizhiEntity> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like("biaoti", keyword).or().like("gongzuoneirong", keyword));
        }
        qw.orderByDesc("addtime");
        return dao.selectPage(new Page<>(current, size), qw);
    }

    public GongzuorizhiEntity get(Long id) { return dao.selectById(id); }

    public boolean save(GongzuorizhiEntity e) {
        if (e.getId() == null) return dao.insert(e) > 0;
        return dao.updateById(e) > 0;
    }

    public boolean delete(Long id) { return dao.deleteById(id) > 0; }
}
