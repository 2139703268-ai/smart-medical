package com.smartmedical.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartmedical.common.Result;
import com.smartmedical.security.LoginUser;
import com.smartmedical.service.MessageService;
import com.smartmedical.service.PrescriptionService;
import com.smartmedical.service.SysUserService;
import com.smartmedical.vo.MessageVO;
import com.smartmedical.vo.UserVO;
import com.smartmedical.vo.PrescriptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pharmacist")
@RequiredArgsConstructor
public class PharmacistController {

    private final PrescriptionService prescriptionService;
    private final SysUserService userService;
    private final MessageService messageService;

    // 工作台统计
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(prescriptionService.getPharmacistStats(loginUser.getUserId()));
    }

    // 待审核处方列表
    @GetMapping("/prescriptions")
    public Result<Page<PrescriptionVO>> getPendingPrescriptions(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(prescriptionService.getPendingPrescriptions(pageNum, pageSize));
    }

    // 已审核处方历史
    @GetMapping("/prescriptions/history")
    public Result<Page<PrescriptionVO>> getReviewedPrescriptions(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(prescriptionService.getReviewedPrescriptions(loginUser.getUserId(), pageNum, pageSize));
    }

    // 处方详情
    @GetMapping("/prescriptions/{id}")
    public Result<PrescriptionVO> getPrescriptionDetail(@PathVariable Long id) {
        return Result.success(prescriptionService.getPrescriptionDetail(id));
    }

    // 审核通过
    @PutMapping("/prescriptions/{id}/approve")
    public Result<Void> approvePrescription(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
        prescriptionService.approvePrescription(id, loginUser.getUserId());
        return Result.success();
    }

    // 审核驳回
    @PutMapping("/prescriptions/{id}/reject")
    public Result<Void> rejectPrescription(@AuthenticationPrincipal LoginUser loginUser,
                                            @PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        prescriptionService.rejectPrescription(id, loginUser.getUserId(), body.get("reason"));
        return Result.success();
    }

    // 获取个人信息
    @GetMapping("/profile")
    public Result<UserVO> getProfile(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(userService.getCurrentUser(loginUser.getUserId()));
    }

    // 修改密码
    @PutMapping("/password")
    public Result<Void> updatePassword(@AuthenticationPrincipal LoginUser loginUser,
                                        @RequestBody Map<String, String> body) {
        userService.updatePassword(loginUser.getUserId(), body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
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
