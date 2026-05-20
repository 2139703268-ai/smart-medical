package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.MedicalRecord;
import com.smartmedical.mapper.MedicalRecordMapper;
import com.smartmedical.service.MedicalRecordService;
import org.springframework.stereotype.Service;

@Service
public class MedicalRecordServiceImpl extends ServiceImpl<MedicalRecordMapper, MedicalRecord> implements MedicalRecordService {

    @Override
    public void saveRecord(MedicalRecord record) {
        this.saveOrUpdate(record);
    }

    @Override
    public MedicalRecord getByAppointmentId(Long appointmentId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getAppointmentId, appointmentId);
        return this.getOne(wrapper);
    }
}
