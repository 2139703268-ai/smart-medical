package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.common.BusinessException;
import com.smartmedical.entity.Department;
import com.smartmedical.entity.DoctorInfo;
import com.smartmedical.entity.OnlineConsultation;
import com.smartmedical.entity.SysUser;
import com.smartmedical.mapper.OnlineConsultationMapper;
import com.smartmedical.service.DepartmentService;
import com.smartmedical.service.DoctorInfoService;
import com.smartmedical.service.MessageService;
import com.smartmedical.service.OnlineConsultationService;
import com.smartmedical.service.SysUserService;
import com.smartmedical.vo.ConsultationVO;
import com.smartmedical.vo.CreateConsultationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnlineConsultationServiceImpl extends ServiceImpl<OnlineConsultationMapper, OnlineConsultation> implements OnlineConsultationService {

    private final SysUserService userService;
    private final DoctorInfoService doctorInfoService;
    private final DepartmentService departmentService;
    private final MessageService messageService;

    @Override
    @Transactional
    public ConsultationVO createConsultation(Long patientId, CreateConsultationDTO dto) {
        // 校验医生存在
        DoctorInfo doctor = doctorInfoService.getById(dto.getDoctorId());
        if (doctor == null) {
            throw new BusinessException("医生不存在");
        }

        OnlineConsultation consultation = new OnlineConsultation();
        consultation.setPatientId(patientId);
        consultation.setDoctorId(dto.getDoctorId());
        consultation.setType(dto.getType());
        consultation.setStatus("PENDING");
        consultation.setSymptom(dto.getSymptom());
        consultation.setSymptomImages(dto.getSymptomImages());
        consultation.setFee(doctor.getRegistrationFee());

        // 视频问诊生成房间ID
        if ("VIDEO".equals(dto.getType())) {
            consultation.setRoomId(UUID.randomUUID().toString().replace("-", ""));
        }

        this.save(consultation);

        // 发送问诊提交通知
        SysUser doctorUser = userService.getById(doctor.getUserId());
        String doctorName = doctorUser != null ? doctorUser.getRealName() : "医生";
        String typeText = "VIDEO".equals(dto.getType()) ? "视频问诊" : "图文问诊";
        messageService.sendMessage(patientId, "问诊已提交",
                "您已成功发起" + typeText + "，等待" + doctorName + "医生接诊。",
                "SYSTEM");

        return getConsultationDetail(consultation.getId());
    }

    @Override
    public ConsultationVO acceptConsultation(Long consultationId, Long doctorId) {
        OnlineConsultation consultation = this.getById(consultationId);
        if (consultation == null) {
            throw new BusinessException("问诊不存在");
        }
        if (!consultation.getDoctorId().equals(doctorId)) {
            throw new BusinessException("无权操作此问诊");
        }
        if (!"PENDING".equals(consultation.getStatus())) {
            throw new BusinessException("问诊状态异常");
        }

        consultation.setStatus("IN_PROGRESS");
        consultation.setStartTime(LocalDateTime.now());
        this.updateById(consultation);

        // 发送医生接诊通知
        DoctorInfo doctorInfo = doctorInfoService.getById(doctorId);
        SysUser doctorUser = doctorInfo != null ? userService.getById(doctorInfo.getUserId()) : null;
        String doctorName = doctorUser != null ? doctorUser.getRealName() : "医生";
        messageService.sendMessage(consultation.getPatientId(), "医生已接诊",
                doctorName + "医生已接受您的问诊请求，可以开始沟通了。",
                "SYSTEM");

        return getConsultationDetail(consultationId);
    }

    @Override
    public void completeConsultation(Long consultationId, Long userId, String role) {
        OnlineConsultation consultation = this.getById(consultationId);
        if (consultation == null) {
            throw new BusinessException("问诊不存在");
        }
        if (!"IN_PROGRESS".equals(consultation.getStatus())) {
            throw new BusinessException("问诊状态异常");
        }

        // 患者或医生都可以结束
        if ("PATIENT".equals(role) && !consultation.getPatientId().equals(userId)) {
            throw new BusinessException("无权操作此问诊");
        }
        if ("DOCTOR".equals(role) && !consultation.getDoctorId().equals(userId)) {
            throw new BusinessException("无权操作此问诊");
        }

        consultation.setStatus("COMPLETED");
        consultation.setEndTime(LocalDateTime.now());
        this.updateById(consultation);

        // 发送问诊结束通知
        if ("DOCTOR".equals(role)) {
            messageService.sendMessage(consultation.getPatientId(), "问诊已结束",
                    "医生已结束本次问诊，感谢您的使用。",
                    "SYSTEM");
        } else {
            DoctorInfo dInfo = doctorInfoService.getById(consultation.getDoctorId());
            SysUser dUser = dInfo != null ? userService.getById(dInfo.getUserId()) : null;
            String dName = dUser != null ? dUser.getRealName() : "医生";
            messageService.sendMessage(consultation.getPatientId(), "问诊已结束",
                    "您与" + dName + "医生的问诊已结束，感谢您的使用。",
                    "SYSTEM");
        }
    }

    @Override
    public void cancelConsultation(Long consultationId, Long userId, String role, String reason) {
        OnlineConsultation consultation = this.getById(consultationId);
        if (consultation == null) {
            throw new BusinessException("问诊不存在");
        }
        if ("COMPLETED".equals(consultation.getStatus())) {
            throw new BusinessException("已完成的问诊不能取消");
        }
        if ("CANCELLED".equals(consultation.getStatus())) {
            throw new BusinessException("问诊已取消");
        }

        consultation.setStatus("CANCELLED");
        consultation.setCancelReason(reason);
        consultation.setEndTime(LocalDateTime.now());
        this.updateById(consultation);
    }

    @Override
    public Page<ConsultationVO> getPatientConsultations(Long patientId, String status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<OnlineConsultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OnlineConsultation::getPatientId, patientId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(OnlineConsultation::getStatus, status);
        }
        wrapper.orderByDesc(OnlineConsultation::getCreateTime);
        Page<OnlineConsultation> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        Page<ConsultationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<ConsultationVO> getDoctorConsultations(Long doctorId, String status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<OnlineConsultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OnlineConsultation::getDoctorId, doctorId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(OnlineConsultation::getStatus, status);
        }
        wrapper.orderByDesc(OnlineConsultation::getCreateTime);
        Page<OnlineConsultation> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        Page<ConsultationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public ConsultationVO getConsultationDetail(Long consultationId) {
        OnlineConsultation consultation = this.getById(consultationId);
        if (consultation == null) {
            throw new BusinessException("问诊不存在");
        }
        return convertToVO(consultation);
    }

    private ConsultationVO convertToVO(OnlineConsultation c) {
        ConsultationVO vo = new ConsultationVO();
        vo.setId(c.getId());
        vo.setPatientId(c.getPatientId());
        vo.setDoctorId(c.getDoctorId());
        vo.setType(c.getType());
        vo.setStatus(c.getStatus());
        vo.setSymptom(c.getSymptom());
        vo.setSymptomImages(c.getSymptomImages());
        vo.setFee(c.getFee());
        vo.setRoomId(c.getRoomId());
        vo.setStartTime(c.getStartTime());
        vo.setEndTime(c.getEndTime());
        vo.setCancelReason(c.getCancelReason());
        vo.setCreateTime(c.getCreateTime());

        // 患者信息
        SysUser patient = userService.getById(c.getPatientId());
        if (patient != null) {
            vo.setPatientName(patient.getRealName());
            vo.setPatientPhone(patient.getPhone());
        }

        // 医生信息
        DoctorInfo doctor = doctorInfoService.getById(c.getDoctorId());
        if (doctor != null) {
            SysUser doctorUser = userService.getById(doctor.getUserId());
            if (doctorUser != null) {
                vo.setDoctorName(doctorUser.getRealName());
            }
            vo.setDoctorTitle(doctor.getTitle());
            vo.setDepartmentId(doctor.getDepartmentId());
            Department dept = departmentService.getById(doctor.getDepartmentId());
            if (dept != null) {
                vo.setDepartmentName(dept.getName());
            }
        }

        return vo;
    }
}
