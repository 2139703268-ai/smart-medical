package com.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmedical.entity.Prescription;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
}
