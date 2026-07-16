package com.jlwl.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jlwl.entity.UsersEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsersDao extends BaseMapper<UsersEntity> {}
