package com.jlwl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jlwl.dao.EmployeeDao;
import com.jlwl.entity.EmployeeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeDao employeeDao;

    public IPage<EmployeeEntity> page(int current, int size, String keyword, Long deptId) {
        QueryWrapper<EmployeeEntity> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like("real_name", keyword).or().like("emp_no", keyword));
        }
        if (deptId != null) qw.eq("dept_id", deptId);
        qw.orderByDesc("create_time");
        return employeeDao.selectPage(new Page<>(current, size), qw);
    }

    public EmployeeEntity get(Long id) {
        return employeeDao.selectById(id);
    }

    public boolean save(EmployeeEntity e) {
        if (e.getId() == null) return employeeDao.insert(e) > 0;
        return employeeDao.updateById(e) > 0;
    }

    public boolean delete(Long id) {
        return employeeDao.deleteById(id) > 0;
    }
}
