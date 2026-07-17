package com.jlwl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jlwl.dao.KbDocumentDao;
import com.jlwl.entity.KbDocumentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KbDocumentService {

    private final KbDocumentDao kbDocumentDao;

    public List<KbDocumentEntity> listByKbId(Long kbId) {
        return kbDocumentDao.listByKbId(kbId);
    }

    public KbDocumentEntity get(Long id) {
        return kbDocumentDao.selectById(id);
    }

    public boolean save(KbDocumentEntity e, Long userId) {
        if (e.getStatus() == null) e.setStatus(0);
        if (e.getFileSize() == null) e.setFileSize(0L);
        if (e.getId() == null) {
            e.setCreateBy(userId);
            e.setCreateTime(LocalDateTime.now());
            return kbDocumentDao.insert(e) > 0;
        }
        e.setUpdateTime(LocalDateTime.now());
        return kbDocumentDao.updateById(e) > 0;
    }

    public boolean updateStatus(Long id, Integer status, String errorMsg) {
        KbDocumentEntity e = new KbDocumentEntity();
        e.setId(id);
        e.setStatus(status);
        e.setErrorMsg(errorMsg);
        e.setUpdateTime(LocalDateTime.now());
        return kbDocumentDao.updateById(e) > 0;
    }

    public boolean delete(Long id) {
        return kbDocumentDao.deleteById(id) > 0;
    }
}
