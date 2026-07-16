package com.jlwl.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jlwl.entity.TokenEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TokenDao extends BaseMapper<TokenEntity> {}
