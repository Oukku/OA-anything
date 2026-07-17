package com.jlwl.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jlwl.entity.KbDocumentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface KbDocumentDao extends BaseMapper<KbDocumentEntity> {
    @Select("SELECT * FROM kb_document WHERE kb_id = #{kbId} AND del_flag = 0 ORDER BY create_time DESC")
    List<KbDocumentEntity> listByKbId(@Param("kbId") Long kbId);
}
