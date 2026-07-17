package com.jlwl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jlwl.dao.KbDao;
import com.jlwl.entity.KbEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KbService {

    private final KbDao kbDao;

    public List<KbEntity> listActive() {
        QueryWrapper<KbEntity> qw = new QueryWrapper<>();
        qw.eq("status", 1).eq("del_flag", 0).orderByAsc("sort_order");
        return kbDao.selectList(qw);
    }

    public List<KbEntity> listAll() {
        QueryWrapper<KbEntity> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0).orderByDesc("create_time");
        return kbDao.selectList(qw);
    }

    public KbEntity get(Long id) {
        return kbDao.selectById(id);
    }

    public boolean save(KbEntity e, Long userId) {
        if (e.getStatus() == null) e.setStatus(1);
        if (e.getId() == null) {
            e.setCreateBy(userId);
            e.setCreateTime(LocalDateTime.now());
            return kbDao.insert(e) > 0;
        }
        e.setUpdateBy(userId);
        e.setUpdateTime(LocalDateTime.now());
        return kbDao.updateById(e) > 0;
    }

    public boolean delete(Long id) {
        return kbDao.deleteById(id) > 0;
    }
}
