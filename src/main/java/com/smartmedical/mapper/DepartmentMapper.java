package com.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmedical.entity.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
