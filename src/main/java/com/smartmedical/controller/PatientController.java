package com.smartmedical.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartmedical.common.Result;
import com.smartmedical.entity.*;
import com.smartmedical.security.LoginUser;
import com.smartmedical.service.*;
import com.smartmedical.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final SysUserService userService;
    private final DepartmentService departmentService;
    private final DoctorInfoService doctorInfoService;
    private final ScheduleService scheduleService;
    private final AppointmentService appointmentService;
    private final EvaluationService evaluationService;
    private final MessageService messageService;
    private final PatientFamilyService familyService;
    private final AnnouncementService announcementService;
    private final OnlineConsultationService consultationService;
    private final ConsultationMessageService consultationMessageService;
    private final PrescriptionService prescriptionService;

    // 获取当前用户信息
    @GetMapping("/user/info")
    public Result<UserVO> getCurrentUser(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(userService.getCurrentUser(loginUser.getUserId()));
    }

    // 更新用户信息
    @PutMapping("/user/info")
    public Result<Void> updateUserInfo(@AuthenticationPrincipal LoginUser loginUser, @RequestBody SysUser user) {
        userService.updateUserInfo(loginUser.getUserId(), user);
        return Result.success();
    }

    // 修改密码
    @PutMapping("/user/password")
    public Result<Void> updatePassword(@AuthenticationPrincipal LoginUser loginUser,
                                       @RequestParam String oldPassword, @RequestParam String newPassword) {
        userService.updatePassword(loginUser.getUserId(), oldPassword, newPassword);
        return Result.success();
    }

    // 获取科室列表
    @GetMapping("/departments")
    public Result<List<Department>> getDepartments() {
        return Result.success(departmentService.getActiveDepartments());
    }

    // 获取热门医生
    @GetMapping("/doctors/hot")
    public Result<List<DoctorVO>> getHotDoctors(@RequestParam(defaultValue = "5") Integer limit) {
        return Result.success(doctorInfoService.getHotDoctors(limit));
    }

    // 获取科室下的医生
    @GetMapping("/doctors/department/{departmentId}")
    public Result<Page<DoctorVO>> getDoctorsByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(doctorInfoService.getDoctorsByDepartment(departmentId, pageNum, pageSize));
    }

    // 获取医生详情
    @GetMapping("/doctors/{doctorId}")
    public Result<DoctorVO> getDoctorDetail(@PathVariable Long doctorId) {
        return Result.success(doctorInfoService.getDoctorDetail(doctorId));
    }

    // 获取医生排班
    @GetMapping("/schedules/doctor/{doctorId}")
    public Result<List<ScheduleVO>> getDoctorSchedules(
            @PathVariable Long doctorId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(scheduleService.getSchedulesByDoctor(doctorId,
                java.time.LocalDate.parse(startDate), java.time.LocalDate.parse(endDate)));
    }

    // 创建预约
    @PostMapping("/appointments")
    public Result<AppointmentVO> createAppointment(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam Long scheduleId,
            @RequestParam(required = false) Long familyId) {
        return Result.success(appointmentService.createAppointment(loginUser.getUserId(), scheduleId, familyId));
    }

    // 取消预约
    @PutMapping("/appointments/{id}/cancel")
    public Result<Void> cancelAppointment(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        appointmentService.cancelAppointment(id, loginUser.getUserId(), "PATIENT");
        return Result.success();
    }

    // 获取我的预约列表
    @GetMapping("/appointments")
    public Result<Page<AppointmentVO>> getMyAppointments(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(appointmentService.getPatientAppointments(loginUser.getUserId(), status, pageNum, pageSize));
    }

    // 获取预约详情
    @GetMapping("/appointments/{id}")
    public Result<AppointmentVO> getAppointmentDetail(@PathVariable Long id) {
        return Result.success(appointmentService.getAppointmentDetail(id));
    }

    // 评价医生
    @PostMapping("/evaluations")
    public Result<Void> saveEvaluation(@AuthenticationPrincipal LoginUser loginUser, @RequestBody Evaluation evaluation) {
        evaluation.setPatientId(loginUser.getUserId());
        evaluationService.saveEvaluation(evaluation);
        return Result.success();
    }

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

    // 获取就诊人列表
    @GetMapping("/family")
    public Result<List<PatientFamily>> getFamilyList(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(familyService.getFamilyList(loginUser.getUserId()));
    }

    // 添加就诊人
    @PostMapping("/family")
    public Result<Void> addFamily(@AuthenticationPrincipal LoginUser loginUser, @RequestBody PatientFamily family) {
        family.setUserId(loginUser.getUserId());
        familyService.addFamily(family);
        return Result.success();
    }

    // 更新就诊人
    @PutMapping("/family/{id}")
    public Result<Void> updateFamily(@PathVariable Long id, @RequestBody PatientFamily family) {
        family.setId(id);
        familyService.updateFamily(family);
        return Result.success();
    }

    // 删除就诊人
    @DeleteMapping("/family/{id}")
    public Result<Void> deleteFamily(@PathVariable Long id) {
        familyService.deleteFamily(id);
        return Result.success();
    }

    // 获取公告列表
    @GetMapping("/announcements")
    public Result<List<Announcement>> getAnnouncements(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(announcementService.getLatestAnnouncements(limit));
    }

    // ========== 在线问诊 ==========

    // 创建问诊
    @PostMapping("/consultations")
    public Result<ConsultationVO> createConsultation(@AuthenticationPrincipal LoginUser loginUser,
                                                     @RequestBody CreateConsultationDTO dto) {
        return Result.success(consultationService.createConsultation(loginUser.getUserId(), dto));
    }

    // 我的问诊列表
    @GetMapping("/consultations")
    public Result<Page<ConsultationVO>> getMyConsultations(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(consultationService.getPatientConsultations(loginUser.getUserId(), status, pageNum, pageSize));
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
        return Result.success(consultationMessageService.sendMessage(
                id, loginUser.getUserId(), "PATIENT", msg.getContent(), msg.getMsgType(), msg.getImageUrl()));
    }

    // 获取聊天记录
    @GetMapping("/consultations/{id}/messages")
    public Result<Page<ConsultationMessageVO>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        return Result.success(consultationMessageService.getMessages(id, pageNum, pageSize));
    }

    // 查看处方
    @GetMapping("/consultations/{id}/prescription")
    public Result<PrescriptionVO> getPrescription(@PathVariable Long id) {
        return Result.success(prescriptionService.getByConsultationId(id));
    }

    // 结束问诊
    @PutMapping("/consultations/{id}/complete")
    public Result<Void> completeConsultation(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        consultationService.completeConsultation(id, loginUser.getUserId(), "PATIENT");
        return Result.success();
    }
}
