package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.PatientFamily;
import com.smartmedical.mapper.PatientFamilyMapper;
import com.smartmedical.service.PatientFamilyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientFamilyServiceImpl extends ServiceImpl<PatientFamilyMapper, PatientFamily> implements PatientFamilyService {

    @Override
    public List<PatientFamily> getFamilyList(Long userId) {
        LambdaQueryWrapper<PatientFamily> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PatientFamily::getUserId, userId);
        return this.list(wrapper);
    }

    @Override
    public void addFamily(PatientFamily family) {
        this.save(family);
    }

    @Override
    public void updateFamily(PatientFamily family) {
        this.updateById(family);
    }

    @Override
    public void deleteFamily(Long id) {
        this.removeById(id);
    }
}
