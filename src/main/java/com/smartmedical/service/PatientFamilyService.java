package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.PatientFamily;

import java.util.List;

public interface PatientFamilyService extends IService<PatientFamily> {

    List<PatientFamily> getFamilyList(Long userId);

    void addFamily(PatientFamily family);

    void updateFamily(PatientFamily family);

    void deleteFamily(Long id);
}
