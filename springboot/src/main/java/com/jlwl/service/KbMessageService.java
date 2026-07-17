package com.jlwl.service;

import com.jlwl.dao.KbMessageDao;
import com.jlwl.entity.KbMessageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KbMessageService {

    private final KbMessageDao kbMessageDao;

    public List<KbMessageEntity> listBySessionId(Long sessionId) {
        return kbMessageDao.listBySessionId(sessionId);
    }

    public boolean save(KbMessageEntity e) {
        if (e.getCreateTime() == null) e.setCreateTime(LocalDateTime.now());
        if (e.getDurationMs() == null) e.setDurationMs(0);
        return kbMessageDao.insert(e) > 0;
    }
}
