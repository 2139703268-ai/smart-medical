package com.smartmedical.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartmedical.common.BusinessException;
import com.smartmedical.common.Result;
import com.smartmedical.entity.*;
import com.smartmedical.security.LoginUser;
import com.smartmedical.service.*;
import com.smartmedical.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final SysUserService userService;
    private final DoctorInfoService doctorInfoService;
    private final ScheduleService scheduleService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final DepartmentService departmentService;
    private final OnlineConsultationService consultationService;
    private final ConsultationMessageService consultationMessageService;
    private final PrescriptionService prescriptionService;
    private final AnnouncementService announcementService;
    private final MessageService messageService;

    private DoctorInfo getDoctorInfo(Long userId) {
        LambdaQueryWrapper<DoctorInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorInfo::getUserId, userId);
        DoctorInfo doctor = doctorInfoService.getOne(wrapper);
        if (doctor == null) {
            throw new BusinessException("医生信息不存在，请联系管理员");
        }
        return doctor;
    }

    // 获取医生信息
    @GetMapping("/info")
    public Result<DoctorVO> getDoctorInfo(@AuthenticationPrincipal LoginUser loginUser) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        return Result.success(doctorInfoService.getDoctorDetail(doctor.getId()));
    }

    // 更新医生信息
    @PutMapping("/info")
    public Result<Void> updateDoctorInfo(@AuthenticationPrincipal LoginUser loginUser, @RequestBody DoctorInfo doctorInfo) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        doctorInfo.setId(doctor.getId());
        doctorInfoService.updateById(doctorInfo);
        return Result.success();
    }

    // 获取排班列表
    @GetMapping("/schedules")
    public Result<List<ScheduleVO>> getSchedules(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        return Result.success(scheduleService.getSchedulesByDoctor(doctor.getId(),
                LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }

    // 添加排班
    @PostMapping("/schedules")
    public Result<Void> addSchedule(@AuthenticationPrincipal LoginUser loginUser, @RequestBody Schedule schedule) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        schedule.setDoctorId(doctor.getId());
        schedule.setDepartmentId(doctor.getDepartmentId());
        scheduleService.addSchedule(schedule);
        return Result.success();
    }

    // 更新排班
    @PutMapping("/schedules/{id}")
    public Result<Void> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        schedule.setId(id);
        scheduleService.updateSchedule(schedule);
        return Result.success();
    }

    // 删除排班
    @DeleteMapping("/schedules/{id}")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return Result.success();
    }

    // 获取预约列表
    @GetMapping("/appointments")
    public Result<Page<AppointmentVO>> getAppointments(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        return Result.success(appointmentService.getDoctorAppointments(doctor.getId(), status, pageNum, pageSize));
    }

    // 更新预约状态（开始就诊）
    @PutMapping("/appointments/{id}/complete")
    public Result<Void> completeAppointment(@PathVariable Long id) {
        appointmentService.completeAppointment(id);
        return Result.success();
    }

    // 取消预约
    @PutMapping("/appointments/{id}/cancel")
    public Result<Void> cancelAppointment(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        appointmentService.cancelAppointment(id, loginUser.getUserId(), "DOCTOR");
        return Result.success();
    }

    // 填写病历
    @PostMapping("/medical-records")
    public Result<Void> saveMedicalRecord(@RequestBody MedicalRecord record) {
        medicalRecordService.saveRecord(record);
        return Result.success();
    }

    // 获取病历
    @GetMapping("/medical-records/appointment/{appointmentId}")
    public Result<MedicalRecord> getMedicalRecord(@PathVariable Long appointmentId) {
        return Result.success(medicalRecordService.getByAppointmentId(appointmentId));
    }

    // 修改密码
    @PutMapping("/password")
    public Result<Void> updatePassword(@AuthenticationPrincipal LoginUser loginUser,
                                       @RequestParam String oldPassword, @RequestParam String newPassword) {
        userService.updatePassword(loginUser.getUserId(), oldPassword, newPassword);
        return Result.success();
    }

    // 获取公告通知
    @GetMapping("/announcements")
    public Result<List<Announcement>> getAnnouncements() {
        return Result.success(announcementService.getLatestAnnouncements(5));
    }

    // ========== 在线问诊 ==========

    // 接诊
    @PutMapping("/consultations/{id}/accept")
    public Result<ConsultationVO> acceptConsultation(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        return Result.success(consultationService.acceptConsultation(id, doctor.getId()));
    }

    // 我的问诊列表
    @GetMapping("/consultations")
    public Result<Page<ConsultationVO>> getMyConsultations(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        return Result.success(consultationService.getDoctorConsultations(doctor.getId(), status, pageNum, pageSize));
    }

    // 问诊详情
    @GetMapping("/consultations/{id}")
    public Result<ConsultationVO> getConsultationDetail(@PathVariable Long id) {
        return Result.success(consultationService.getConsultationDetail(id));
    }

    // 发送消息
    @PostMapping("/consultations/{id}/messages")
    public Result<ConsultationMessageVO> sendMessage(@AuthenticationPrincipal LoginUser loginUser,
                                                     @PathVariable Long id,
                                                     @RequestBody ConsultationMessage msg) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        return Result.success(consultationMessageService.sendMessage(
                id, doctor.getId(), "DOCTOR", msg.getContent(), msg.getMsgType(), msg.getImageUrl()));
    }

    // 获取聊天记录
    @GetMapping("/consultations/{id}/messages")
    public Result<Page<ConsultationMessageVO>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        return Result.success(consultationMessageService.getMessages(id, pageNum, pageSize));
    }

    // 开具处方
    @PostMapping("/consultations/{id}/prescription")
    public Result<PrescriptionVO> issuePrescription(@AuthenticationPrincipal LoginUser loginUser,
                                                    @PathVariable Long id,
                                                    @RequestBody Prescription prescription) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        return Result.success(prescriptionService.issuePrescription(
                id, doctor.getId(), prescription.getDiagnosis(), prescription.getContent(), prescription.getAdvice()));
    }

    // 修改处方
    @PutMapping("/consultations/{id}/prescription")
    public Result<PrescriptionVO> updatePrescription(@AuthenticationPrincipal LoginUser loginUser,
                                                     @PathVariable Long id,
                                                     @RequestBody Prescription prescription) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        return Result.success(prescriptionService.updatePrescription(
                id, doctor.getId(), prescription.getDiagnosis(), prescription.getContent(), prescription.getAdvice()));
    }

    // 结束问诊
    @PutMapping("/consultations/{id}/complete")
    public Result<Void> completeConsultation(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        consultationService.completeConsultation(id, doctor.getId(), "DOCTOR");
        return Result.success();
    }

    // ========== 预约处方 ==========

    // 从预约开具处方
    @PostMapping("/appointments/{id}/prescription")
    public Result<PrescriptionVO> issuePrescriptionByAppointment(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @RequestBody Prescription prescription) {
        DoctorInfo doctor = getDoctorInfo(loginUser.getUserId());
        // 获取预约信息以获取patientId
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        return Result.success(prescriptionService.issuePrescriptionByAppointment(
                id, appointment.getPatientId(), doctor.getId(),
                prescription.getDiagnosis(), prescription.getContent(), prescription.getAdvice()));
    }

    // 获取预约的处方
    @GetMapping("/appointments/{id}/prescription")
    public Result<PrescriptionVO> getPrescriptionByAppointment(@PathVariable Long id) {
        return Result.success(prescriptionService.getByAppointmentId(id));
    }

    // ========== 消息通知 ==========

    // 获取我的消息
    @GetMapping("/messages")
    public Result<Page<MessageVO>> getMyMessages(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(messageService.getUserMessageVOs(loginUser.getUserId(), pageNum, pageSize));
    }

    // 获取未读消息数量
    @GetMapping("/messages/unread-count")
    public Result<Long> getUnreadCount(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(messageService.getUnreadCount(loginUser.getUserId()));
    }

    // 标记消息已读
    @PutMapping("/messages/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    // 标记所有消息已读
    @PutMapping("/messages/read-all")
    public Result<Void> markAllAsRead(@AuthenticationPrincipal LoginUser loginUser) {
        messageService.markAllAsRead(loginUser.getUserId());
        return Result.success();
    }
}
