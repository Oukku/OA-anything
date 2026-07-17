package com.jlwl.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jlwl.entity.KbMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface KbMessageDao extends BaseMapper<KbMessageEntity> {
    @Select("SELECT * FROM kb_message WHERE session_id = #{sessionId} ORDER BY create_time ASC")
    List<KbMessageEntity> listBySessionId(@Param("sessionId") Long sessionId);
}
