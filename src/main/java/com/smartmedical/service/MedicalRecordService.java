package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.MedicalRecord;

public interface MedicalRecordService extends IService<MedicalRecord> {

    void saveRecord(MedicalRecord record);

    MedicalRecord getByAppointmentId(Long appointmentId);
}
