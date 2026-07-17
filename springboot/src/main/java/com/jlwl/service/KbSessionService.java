package com.jlwl.service;

import com.jlwl.dao.KbMessageDao;
import com.jlwl.dao.KbSessionDao;
import com.jlwl.entity.KbMessageEntity;
import com.jlwl.entity.KbSessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KbSessionService {

    private final KbSessionDao kbSessionDao;
    private final KbMessageDao kbMessageDao;

    public List<KbSessionEntity> listByUserId(Long userId) {
        return kbSessionDao.listByUserId(userId);
    }

    public KbSessionEntity get(Long id) {
        return kbSessionDao.selectById(id);
    }

    public boolean save(KbSessionEntity e) {
        e.setUpdateTime(LocalDateTime.now());
        if (e.getId() == null) {
            e.setCreateTime(LocalDateTime.now());
            return kbSessionDao.insert(e) > 0;
        }
        return kbSessionDao.updateById(e) > 0;
    }

    public boolean updateTitle(Long id, String title) {
        KbSessionEntity e = new KbSessionEntity();
        e.setId(id);
        e.setTitle(title);
        e.setUpdateTime(LocalDateTime.now());
        return kbSessionDao.updateById(e) > 0;
    }

    @Transactional
    public boolean delete(Long id) {
        kbMessageDao.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<KbMessageEntity>().eq("session_id", id));
        return kbSessionDao.deleteById(id) > 0;
    }
}
