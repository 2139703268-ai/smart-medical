package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.common.BusinessException;
import com.smartmedical.entity.*;
import com.smartmedical.mapper.AppointmentMapper;
import com.smartmedical.service.*;
import com.smartmedical.service.PatientFamilyService;
import com.smartmedical.service.MessageService;
import com.smartmedical.vo.AppointmentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private final ScheduleService scheduleService;
    private final SysUserService userService;
    private final DoctorInfoService doctorInfoService;
    private final DepartmentService departmentService;
    private final PatientFamilyService patientFamilyService;
    private final MessageService messageService;

    @Override
    @Transactional
    public AppointmentVO createAppointment(Long patientId, Long scheduleId, Long familyId) {
        // 获取排班信息
        Schedule schedule = scheduleService.getById(scheduleId);
        if (schedule == null || schedule.getStatus() != 1) {
            throw new BusinessException("该时段不可预约");
        }
        if (schedule.getRemainingNumber() <= 0) {
            throw new BusinessException("该时段号源已满");
        }

        // 使用乐观锁更新剩余号源
        schedule.setRemainingNumber(schedule.getRemainingNumber() - 1);
        boolean updated = scheduleService.updateById(schedule);
        if (!updated) {
            throw new BusinessException("预约失败，请重试");
        }

        // 获取医生信息
        DoctorInfo doctor = doctorInfoService.getById(schedule.getDoctorId());

        // 创建预约记录
        Appointment appointment = new Appointment();
        appointment.setPatientId(familyId != null ? familyId : patientId);
        appointment.setDoctorId(schedule.getDoctorId());
        appointment.setDepartmentId(schedule.getDepartmentId());
        appointment.setScheduleId(scheduleId);
        appointment.setAppointmentTime(LocalDateTime.now());
        appointment.setTimeSlot(schedule.getTimeSlot());
        appointment.setFee(doctor.getRegistrationFee());
        appointment.setStatus("PAID");
        this.save(appointment);

        // 更新医生总预约量
        doctor.setTotalAppointments(doctor.getTotalAppointments() + 1);
        doctorInfoService.updateById(doctor);

        // 发送预约成功通知
        SysUser doctorUser = userService.getById(doctor.getUserId());
        String doctorName = doctorUser != null ? doctorUser.getRealName() : "医生";
        Department dept = departmentService.getById(schedule.getDepartmentId());
        String deptName = dept != null ? dept.getName() : "";
        messageService.sendMessage(patientId, "预约成功",
                "您已成功预约" + deptName + doctorName + "医生，就诊时间：" + schedule.getTimeSlot(),
                "APPOINTMENT");

        return getAppointmentDetail(appointment.getId());
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId, String role) {
        Appointment appointment = this.getById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        if ("COMPLETED".equals(appointment.getStatus())) {
            throw new BusinessException("已完成的预约不能取消");
        }
        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new BusinessException("预约已取消");
        }

        // 释放号源
        Schedule schedule = scheduleService.getById(appointment.getScheduleId());
        schedule.setRemainingNumber(schedule.getRemainingNumber() + 1);
        scheduleService.updateById(schedule);

        // 更新预约状态
        appointment.setStatus("CANCELLED");
        appointment.setCancelTime(LocalDateTime.now());
        this.updateById(appointment);

        // 发送取消通知
        Long notifyUserId = userId != null ? userId : appointment.getPatientId();
        messageService.sendMessage(notifyUserId, "预约已取消",
                "您的预约已取消，如需就诊请重新预约。",
                "APPOINTMENT");
    }

    @Override
    public void completeAppointment(Long appointmentId) {
        Appointment appointment = this.getById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        appointment.setStatus("COMPLETED");
        this.updateById(appointment);

        // 发送就诊完成通知
        messageService.sendMessage(appointment.getPatientId(), "就诊完成",
                "您的就诊已完成，祝您早日康复！",
                "APPOINTMENT");
    }

    @Override
    public Page<AppointmentVO> getPatientAppointments(Long patientId, String status, Integer pageNum, Integer pageSize) {
        // 查询本人和所有就诊人的预约
        List<Long> patientIds = new ArrayList<>();
        patientIds.add(patientId);
        List<PatientFamily> familyList = patientFamilyService.getFamilyList(patientId);
        for (PatientFamily f : familyList) {
            patientIds.add(f.getId());
        }

        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Appointment::getPatientId, patientIds);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Appointment::getStatus, status);
        }
        wrapper.orderByDesc(Appointment::getCreateTime);
        Page<Appointment> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        Page<AppointmentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(a -> convertToVO(a))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<AppointmentVO> getDoctorAppointments(Long doctorId, String status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getDoctorId, doctorId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Appointment::getStatus, status);
        }
        wrapper.orderByDesc(Appointment::getCreateTime);
        Page<Appointment> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        Page<AppointmentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(a -> convertToVO(a))
                .collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    public AppointmentVO getAppointmentDetail(Long appointmentId) {
        Appointment appointment = this.getById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        return convertToVO(appointment);
    }

    @Override
    public void autoCancelExpiredAppointments() {
        // 自动取消超过15分钟未支付的预约
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getStatus, "PENDING")
               .lt(Appointment::getCreateTime, LocalDateTime.now().minusMinutes(15));
        this.list(wrapper).forEach(appointment -> {
            cancelAppointment(appointment.getId(), null, "SYSTEM");
        });
    }

    private AppointmentVO convertToVO(Appointment appointment) {
        AppointmentVO vo = new AppointmentVO();
        vo.setId(appointment.getId());
        vo.setPatientId(appointment.getPatientId());
        vo.setDoctorId(appointment.getDoctorId());
        vo.setDepartmentId(appointment.getDepartmentId());
        vo.setScheduleId(appointment.getScheduleId());
        vo.setAppointmentTime(appointment.getAppointmentTime());
        vo.setTimeSlot(appointment.getTimeSlot());
        vo.setQueueNumber(appointment.getQueueNumber());
        vo.setFee(appointment.getFee());
        vo.setStatus(appointment.getStatus());
        vo.setCancelReason(appointment.getCancelReason());
        vo.setPayTime(appointment.getPayTime());
        vo.setCancelTime(appointment.getCancelTime());
        vo.setCreateTime(appointment.getCreateTime());

        // 获取患者信息（先查sys_user，再查patient_family）
        SysUser patient = userService.getById(appointment.getPatientId());
        if (patient != null) {
            vo.setPatientName(patient.getRealName());
            vo.setPatientPhone(patient.getPhone());
        } else {
            PatientFamily family = patientFamilyService.getById(appointment.getPatientId());
            if (family != null) {
                vo.setPatientName(family.getName());
                vo.setPatientPhone(family.getPhone());
            }
        }

        // 获取医生信息
        DoctorInfo doctor = doctorInfoService.getById(appointment.getDoctorId());
        if (doctor != null) {
            SysUser doctorUser = userService.getById(doctor.getUserId());
            if (doctorUser != null) {
                vo.setDoctorName(doctorUser.getRealName());
            }
            vo.setDoctorTitle(doctor.getTitle());
        }

        // 获取科室信息
        Department dept = departmentService.getById(appointment.getDepartmentId());
        if (dept != null) {
            vo.setDepartmentName(dept.getName());
        }

        return vo;
    }
}
