package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.Prescription;
import com.smartmedical.vo.PrescriptionVO;

import java.util.Map;

public interface PrescriptionService extends IService<Prescription> {

    PrescriptionVO issuePrescription(Long consultationId, Long doctorId, String diagnosis, String content, String advice);

    PrescriptionVO issuePrescriptionByAppointment(Long appointmentId, Long patientId, Long doctorId, String diagnosis, String content, String advice);

    PrescriptionVO updatePrescription(Long consultationId, Long doctorId, String diagnosis, String content, String advice);

    void approvePrescription(Long prescriptionId, Long pharmacistId);

    void rejectPrescription(Long prescriptionId, Long pharmacistId, String reason);

    PrescriptionVO getByConsultationId(Long consultationId);

    PrescriptionVO getByAppointmentId(Long appointmentId);

    Page<PrescriptionVO> getPendingPrescriptions(Integer pageNum, Integer pageSize);

    Page<PrescriptionVO> getReviewedPrescriptions(Long pharmacistId, Integer pageNum, Integer pageSize);

    PrescriptionVO getPrescriptionDetail(Long id);

    Map<String, Object> getPharmacistStats(Long pharmacistId);
}
