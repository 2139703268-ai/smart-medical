package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.Department;

import java.util.List;

public interface DepartmentService extends IService<Department> {

    List<Department> getActiveDepartments();
}
