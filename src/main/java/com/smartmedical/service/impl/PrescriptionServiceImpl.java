package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.common.BusinessException;
import com.smartmedical.entity.DoctorInfo;
import com.smartmedical.entity.OnlineConsultation;
import com.smartmedical.entity.Prescription;
import com.smartmedical.entity.SysUser;
import com.smartmedical.mapper.PrescriptionMapper;
import com.smartmedical.service.DoctorInfoService;
import com.smartmedical.service.MessageService;
import com.smartmedical.service.OnlineConsultationService;
import com.smartmedical.service.PrescriptionService;
import com.smartmedical.service.SysUserService;
import com.smartmedical.vo.PrescriptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription> implements PrescriptionService {

    private final OnlineConsultationService consultationService;
    private final SysUserService userService;
    private final DoctorInfoService doctorInfoService;
    private final MessageService messageService;

    @Override
    @Transactional
    public PrescriptionVO issuePrescription(Long consultationId, Long doctorId, String diagnosis, String content, String advice) {
        OnlineConsultation consultation = consultationService.getById(consultationId);
        if (consultation == null) {
            throw new BusinessException("问诊不存在");
        }
        if (!consultation.getDoctorId().equals(doctorId)) {
            throw new BusinessException("无权操作此问诊");
        }

        // 检查是否已有处方
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getConsultationId, consultationId);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("已开具处方，请勿重复操作");
        }

        Prescription prescription = new Prescription();
        prescription.setConsultationId(consultationId);
        prescription.setPatientId(consultation.getPatientId());
        prescription.setDoctorId(doctorId);
        prescription.setDiagnosis(diagnosis);
        prescription.setContent(content);
        prescription.setAdvice(advice);
        prescription.setStatus("PENDING_REVIEW");
        this.save(prescription);

        // 通知患者
        DoctorInfo doctor = doctorInfoService.getById(doctorId);
        SysUser doctorUser = doctor != null ? userService.getById(doctor.getUserId()) : null;
        String doctorName = doctorUser != null ? doctorUser.getRealName() : "医生";
        messageService.sendMessage(consultation.getPatientId(), "处方已开具",
                doctorName + "医生已为您开具处方，请查看处方详情。",
                "SYSTEM");

        // 通知所有药师有新处方待审核
        LambdaQueryWrapper<SysUser> pharmacistWrapper = new LambdaQueryWrapper<>();
        pharmacistWrapper.eq(SysUser::getRole, "PHARMACIST");
        java.util.List<SysUser> pharmacists = userService.list(pharmacistWrapper);
        SysUser patient = userService.getById(consultation.getPatientId());
        String patientName = patient != null ? patient.getRealName() : "患者";
        for (SysUser pharmacist : pharmacists) {
            messageService.sendMessage(pharmacist.getId(), "新处方待审核",
                    patientName + "的处方等待审核，请及时处理。",
                    "SYSTEM");
        }

        return convertToVO(prescription);
    }

    @Override
    @Transactional
    public PrescriptionVO issuePrescriptionByAppointment(Long appointmentId, Long patientId, Long doctorId, String diagnosis, String content, String advice) {
        // 检查是否已有处方
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getAppointmentId, appointmentId);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("已开具处方，请勿重复操作");
        }

        Prescription prescription = new Prescription();
        prescription.setAppointmentId(appointmentId);
        prescription.setPatientId(patientId);
        prescription.setDoctorId(doctorId);
        prescription.setDiagnosis(diagnosis);
        prescription.setContent(content);
        prescription.setAdvice(advice);
        prescription.setStatus("PENDING_REVIEW");
        this.save(prescription);

        // 通知患者
        DoctorInfo doctor = doctorInfoService.getById(doctorId);
        SysUser doctorUser = doctor != null ? userService.getById(doctor.getUserId()) : null;
        String doctorName = doctorUser != null ? doctorUser.getRealName() : "医生";
        messageService.sendMessage(patientId, "处方已开具",
                doctorName + "医生已为您开具处方，请查看处方详情。",
                "SYSTEM");

        // 通知所有药师有新处方待审核
        LambdaQueryWrapper<SysUser> pharmacistWrapper = new LambdaQueryWrapper<>();
        pharmacistWrapper.eq(SysUser::getRole, "PHARMACIST");
        java.util.List<SysUser> pharmacists = userService.list(pharmacistWrapper);
        SysUser patient = userService.getById(patientId);
        String patientName = patient != null ? patient.getRealName() : "患者";
        for (SysUser pharmacist : pharmacists) {
            messageService.sendMessage(pharmacist.getId(), "新处方待审核",
                    patientName + "的处方等待审核，请及时处理。",
                    "SYSTEM");
        }

        return convertToVO(prescription);
    }

    @Override
    @Transactional
    public PrescriptionVO updatePrescription(Long consultationId, Long doctorId, String diagnosis, String content, String advice) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getConsultationId, consultationId);
        Prescription prescription = this.getOne(wrapper);

        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        if (!prescription.getDoctorId().equals(doctorId)) {
            throw new BusinessException("无权修改此处方");
        }
        if ("APPROVED".equals(prescription.getStatus())) {
            throw new BusinessException("已审核通过的处方不能修改");
        }

        prescription.setDiagnosis(diagnosis);
        prescription.setContent(content);
        prescription.setAdvice(advice);
        prescription.setStatus("PENDING_REVIEW");
        prescription.setPharmacistId(null);
        prescription.setReviewTime(null);
        prescription.setRejectReason(null);
        this.updateById(prescription);

        return convertToVO(prescription);
    }

    @Override
    public void approvePrescription(Long prescriptionId, Long pharmacistId) {
        Prescription prescription = this.getById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        if (!"PENDING_REVIEW".equals(prescription.getStatus()) && !"REJECTED".equals(prescription.getStatus())) {
            throw new BusinessException("处方状态异常");
        }

        prescription.setStatus("APPROVED");
        prescription.setPharmacistId(pharmacistId);
        prescription.setReviewTime(LocalDateTime.now());
        prescription.setRejectReason(null);
        this.updateById(prescription);

        // 通知患者
        messageService.sendMessage(prescription.getPatientId(), "处方审核通过",
                "您的处方已通过药师审核，可以前往药房取药。",
                "SYSTEM");

        // 通知医生
        DoctorInfo doctorInfo = doctorInfoService.getById(prescription.getDoctorId());
        if (doctorInfo != null) {
            SysUser pharmacist = userService.getById(pharmacistId);
            String pharmacistName = pharmacist != null ? pharmacist.getRealName() : "药师";
            messageService.sendMessage(doctorInfo.getUserId(), "处方审核通过",
                    pharmacistName + "药师已审核通过您开具的处方。",
                    "SYSTEM");
        }
    }

    @Override
    public void rejectPrescription(Long prescriptionId, Long pharmacistId, String reason) {
        Prescription prescription = this.getById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        if (!"PENDING_REVIEW".equals(prescription.getStatus())) {
            throw new BusinessException("处方状态异常");
        }

        prescription.setStatus("REJECTED");
        prescription.setPharmacistId(pharmacistId);
        prescription.setReviewTime(LocalDateTime.now());
        prescription.setRejectReason(reason);
        this.updateById(prescription);

        // 通知患者
        messageService.sendMessage(prescription.getPatientId(), "处方被驳回",
                "您的处方未通过药师审核，原因：" + reason + "。请联系医生重新开具。",
                "SYSTEM");

        // 通知医生
        DoctorInfo doctorInfo = doctorInfoService.getById(prescription.getDoctorId());
        if (doctorInfo != null) {
            SysUser pharmacist = userService.getById(pharmacistId);
            String pharmacistName = pharmacist != null ? pharmacist.getRealName() : "药师";
            messageService.sendMessage(doctorInfo.getUserId(), "处方被驳回",
                    pharmacistName + "药师驳回了您开具的处方，原因：" + reason,
                    "SYSTEM");
        }
    }

    @Override
    public PrescriptionVO getByConsultationId(Long consultationId) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getConsultationId, consultationId);
        Prescription prescription = this.getOne(wrapper);
        if (prescription == null) {
            return null;
        }
        return convertToVO(prescription);
    }

    @Override
    public PrescriptionVO getByAppointmentId(Long appointmentId) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getAppointmentId, appointmentId);
        Prescription prescription = this.getOne(wrapper);
        if (prescription == null) {
            return null;
        }
        return convertToVO(prescription);
    }

    @Override
    public Page<PrescriptionVO> getPendingPrescriptions(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getStatus, "PENDING_REVIEW")
               .or()
               .eq(Prescription::getStatus, "REJECTED");
        wrapper.orderByDesc(Prescription::getCreateTime);
        Page<Prescription> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        Page<PrescriptionVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<PrescriptionVO> getReviewedPrescriptions(Long pharmacistId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Prescription::getPharmacistId, pharmacistId)
               .in(Prescription::getStatus, "APPROVED", "REJECTED");
        wrapper.orderByDesc(Prescription::getReviewTime);
        Page<Prescription> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        Page<PrescriptionVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public PrescriptionVO getPrescriptionDetail(Long id) {
        Prescription prescription = this.getById(id);
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        return convertToVO(prescription);
    }

    @Override
    public Map<String, Object> getPharmacistStats(Long pharmacistId) {
        Map<String, Object> stats = new HashMap<>();

        // 待审核数
        LambdaQueryWrapper<Prescription> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Prescription::getStatus, "PENDING_REVIEW");
        stats.put("pendingCount", this.count(pendingWrapper));

        // 今日已审核数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<Prescription> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(Prescription::getPharmacistId, pharmacistId)
                    .ge(Prescription::getReviewTime, todayStart);
        stats.put("todayReviewed", this.count(todayWrapper));

        // 本月审核数
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LambdaQueryWrapper<Prescription> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.eq(Prescription::getPharmacistId, pharmacistId)
                    .ge(Prescription::getReviewTime, monthStart);
        stats.put("monthReviewed", this.count(monthWrapper));

        // 累计审核数
        LambdaQueryWrapper<Prescription> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Prescription::getPharmacistId, pharmacistId);
        stats.put("totalReviewed", this.count(totalWrapper));

        return stats;
    }

    private PrescriptionVO convertToVO(Prescription p) {
        PrescriptionVO vo = new PrescriptionVO();
        vo.setId(p.getId());
        vo.setConsultationId(p.getConsultationId());
        vo.setAppointmentId(p.getAppointmentId());
        vo.setPatientId(p.getPatientId());
        vo.setDoctorId(p.getDoctorId());
        vo.setDiagnosis(p.getDiagnosis());
        vo.setContent(p.getContent());
        vo.setAdvice(p.getAdvice());
        vo.setStatus(p.getStatus());
        vo.setPharmacistId(p.getPharmacistId());
        vo.setReviewTime(p.getReviewTime());
        vo.setRejectReason(p.getRejectReason());
        vo.setCreateTime(p.getCreateTime());

        // 患者信息
        SysUser patient = userService.getById(p.getPatientId());
        if (patient != null) {
            vo.setPatientName(patient.getRealName());
        }

        // 医生信息
        com.smartmedical.entity.DoctorInfo doctor = doctorInfoService.getById(p.getDoctorId());
        if (doctor != null) {
            SysUser doctorUser = userService.getById(doctor.getUserId());
            if (doctorUser != null) {
                vo.setDoctorName(doctorUser.getRealName());
            }
            vo.setDoctorTitle(doctor.getTitle());
        }

        // 药师信息
        if (p.getPharmacistId() != null) {
            SysUser pharmacist = userService.getById(p.getPharmacistId());
            if (pharmacist != null) {
                vo.setPharmacistName(pharmacist.getRealName());
            }
        }

        return vo;
    }
}
