package com.jlwl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jlwl.dao.DeptDao;
import com.jlwl.entity.DeptEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final DeptDao deptDao;

    public List<DeptEntity> list() {
        return deptDao.selectList(new QueryWrapper<DeptEntity>().orderByAsc("sort_order"));
    }

    public IPage<DeptEntity> page(int current, int size, String keyword) {
        QueryWrapper<DeptEntity> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like("dept_name", keyword);
        }
        qw.orderByAsc("sort_order");
        return deptDao.selectPage(new Page<>(current, size), qw);
    }

    public boolean save(DeptEntity dept) {
        if (dept.getId() == null) return deptDao.insert(dept) > 0;
        return deptDao.updateById(dept) > 0;
    }

    public boolean delete(Long id) {
        return deptDao.deleteById(id) > 0;
    }
}
