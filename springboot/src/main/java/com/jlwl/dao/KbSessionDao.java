package com.jlwl.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jlwl.entity.KbSessionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface KbSessionDao extends BaseMapper<KbSessionEntity> {
    @Select("SELECT * FROM kb_session WHERE user_id = #{userId} AND del_flag = 0 ORDER BY update_time DESC")
    List<KbSessionEntity> listByUserId(@Param("userId") Long userId);
}
