package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.OnlineConsultation;
import com.smartmedical.vo.ConsultationVO;
import com.smartmedical.vo.CreateConsultationDTO;

public interface OnlineConsultationService extends IService<OnlineConsultation> {

    ConsultationVO createConsultation(Long patientId, CreateConsultationDTO dto);

    ConsultationVO acceptConsultation(Long consultationId, Long doctorId);

    void completeConsultation(Long consultationId, Long userId, String role);

    void cancelConsultation(Long consultationId, Long userId, String role, String reason);

    Page<ConsultationVO> getPatientConsultations(Long patientId, String status, Integer pageNum, Integer pageSize);

    Page<ConsultationVO> getDoctorConsultations(Long doctorId, String status, Integer pageNum, Integer pageSize);

    ConsultationVO getConsultationDetail(Long consultationId);
}
