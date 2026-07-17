package com.jlwl.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jlwl.entity.AiConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiConfigDao extends BaseMapper<AiConfigEntity> {
    @Select("SELECT * FROM ai_config WHERE is_default = 1 LIMIT 1")
    AiConfigEntity getDefault();

    @Select("SELECT COUNT(*) FROM ai_config")
    long countAll();
}
