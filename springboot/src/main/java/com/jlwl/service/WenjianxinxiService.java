package com.jlwl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jlwl.dao.WenjianxinxiDao;
import com.jlwl.entity.WenjianxinxiEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WenjianxinxiService {

    private final WenjianxinxiDao dao;

    public IPage<WenjianxinxiEntity> page(int current, int size, String keyword, Long folderId, String sfsh) {
        QueryWrapper<WenjianxinxiEntity> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like("biaoti", keyword).or().like("wenjianneirong", keyword));
        }
        if (sfsh != null && !sfsh.isEmpty()) {
            qw.eq("sfsh", sfsh);
        }
        qw.orderByDesc("addtime");
        return dao.selectPage(new Page<>(current, size), qw);
    }

    public WenjianxinxiEntity get(Long id) { return dao.selectById(id); }

    public boolean save(WenjianxinxiEntity e) {
        if (e.getId() == null) return dao.insert(e) > 0;
        return dao.updateById(e) > 0;
    }

    public boolean delete(Long id) { return dao.deleteById(id) > 0; }
}
