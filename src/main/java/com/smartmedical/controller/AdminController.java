package com.smartmedical.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartmedical.common.Result;
import com.smartmedical.entity.*;
import com.smartmedical.service.*;
import com.smartmedical.vo.DoctorVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SysUserService userService;
    private final DepartmentService departmentService;
    private final DoctorInfoService doctorInfoService;
    private final AppointmentService appointmentService;
    private final AnnouncementService announcementService;
    private final PasswordEncoder passwordEncoder;

    // ========== 科室管理 ==========

    @GetMapping("/departments")
    public Result<List<Department>> getDepartments() {
        return Result.success(departmentService.list());
    }

    @PostMapping("/departments")
    public Result<Void> addDepartment(@RequestBody Department department) {
        departmentService.save(department);
        return Result.success();
    }

    @PutMapping("/departments/{id}")
    public Result<Void> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id);
        departmentService.updateById(department);
        return Result.success();
    }

    @DeleteMapping("/departments/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.removeById(id);
        return Result.success();
    }

    // ========== 医生管理 ==========

    @GetMapping("/doctors")
    public Result<Page<DoctorVO>> getDoctors(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        if (departmentId != null) {
            return Result.success(doctorInfoService.getDoctorsByDepartment(departmentId, pageNum, pageSize));
        }
        Page<DoctorInfo> page = doctorInfoService.page(new Page<>(pageNum, pageSize));
        Page<DoctorVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(doctor -> doctorInfoService.getDoctorDetail(doctor.getId()))
                .toList());
        return Result.success(voPage);
    }

    @PostMapping("/doctors")
    public Result<Void> addDoctor(@RequestBody Map<String, Object> params) {
        // 创建用户账号
        SysUser user = new SysUser();
        user.setUsername((String) params.get("username"));
        String password = params.containsKey("password") && params.get("password") != null
                ? (String) params.get("password") : "123456";
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName((String) params.get("realName"));
        user.setPhone((String) params.get("phone"));
        user.setRole("DOCTOR");
        user.setStatus(1);
        userService.save(user);

        // 创建医生信息
        DoctorInfo doctor = new DoctorInfo();
        doctor.setUserId(user.getId());
        doctor.setDepartmentId(Long.valueOf(params.get("departmentId").toString()));
        doctor.setTitle((String) params.get("title"));
        doctor.setSpecialty((String) params.get("specialty"));
        doctor.setIntroduction((String) params.get("introduction"));
        doctorInfoService.save(doctor);

        return Result.success();
    }

    @PutMapping("/doctors/{id}")
    public Result<Void> updateDoctor(@PathVariable Long id, @RequestBody DoctorInfo doctorInfo) {
        doctorInfo.setId(id);
        doctorInfoService.updateById(doctorInfo);
        return Result.success();
    }

    @DeleteMapping("/doctors/{id}")
    public Result<Void> deleteDoctor(@PathVariable Long id) {
        DoctorInfo doctor = doctorInfoService.getById(id);
        if (doctor != null) {
            SysUser user = userService.getById(doctor.getUserId());
            if (user != null) {
                user.setStatus(0);
                userService.updateById(user);
            }
        }
        doctorInfoService.removeById(id);
        return Result.success();
    }

    // ========== 患者管理 ==========

    @GetMapping("/patients")
    public Result<Page<SysUser>> getPatients(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getRole, "PATIENT");
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getRealName, keyword).or().like(SysUser::getPhone, keyword));
        }
        return Result.success(userService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    @PutMapping("/patients/{id}/blacklist")
    public Result<Void> toggleBlacklist(@PathVariable Long id, @RequestParam Integer status) {
        SysUser user = userService.getById(id);
        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }

    // ========== 公告管理 ==========

    @GetMapping("/announcements")
    public Result<List<Announcement>> getAnnouncements() {
        return Result.success(announcementService.list());
    }

    @PostMapping("/announcements")
    public Result<Void> addAnnouncement(@RequestBody Announcement announcement) {
        announcementService.save(announcement);
        return Result.success();
    }

    @PutMapping("/announcements/{id}")
    public Result<Void> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.updateById(announcement);
        return Result.success();
    }

    @DeleteMapping("/announcements/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success();
    }

    // ========== 药师管理 ==========

    @GetMapping("/pharmacists")
    public Result<Page<SysUser>> getPharmacists(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getRole, "PHARMACIST");
        return Result.success(userService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    @PostMapping("/pharmacists")
    public Result<Void> addPharmacist(@RequestBody Map<String, Object> params) {
        SysUser user = new SysUser();
        user.setUsername((String) params.get("username"));
        String password = params.containsKey("password") && params.get("password") != null
                ? (String) params.get("password") : "123456";
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName((String) params.get("realName"));
        user.setPhone((String) params.get("phone"));
        user.setRole("PHARMACIST");
        user.setStatus(1);
        userService.save(user);
        return Result.success();
    }

    @DeleteMapping("/pharmacists/{id}")
    public Result<Void> deletePharmacist(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) {
            user.setStatus(0);
            userService.updateById(user);
        }
        return Result.success();
    }

    // ========== 数据统计 ==========

    @GetMapping("/statistics/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalPatients", userService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "PATIENT")));
        stats.put("totalDoctors", doctorInfoService.count());
        stats.put("totalAppointments", appointmentService.count());
        stats.put("totalDepartments", departmentService.count());
        return Result.success(stats);
    }

    @GetMapping("/statistics/appointments/trend")
    public Result<List<Map<String, Object>>> getAppointmentTrend() {
        // 近7天预约趋势
        List<Map<String, Object>> trend = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = java.time.LocalDate.now().minusDays(i);
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("date", date.toString());
            LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.apply("DATE(create_time) = {0}", date);
            item.put("count", appointmentService.count(wrapper));
            trend.add(item);
        }
        return Result.success(trend);
    }

    @GetMapping("/statistics/departments/distribution")
    public Result<List<Map<String, Object>>> getDepartmentDistribution() {
        List<Map<String, Object>> distribution = new java.util.ArrayList<>();
        List<Department> departments = departmentService.list();
        for (Department dept : departments) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("name", dept.getName());
            LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Appointment::getDepartmentId, dept.getId());
            item.put("value", appointmentService.count(wrapper));
            distribution.add(item);
        }
        return Result.success(distribution);
    }

    @GetMapping("/statistics/doctors/ranking")
    public Result<List<Map<String, Object>>> getDoctorRanking() {
        List<Map<String, Object>> ranking = new java.util.ArrayList<>();
        LambdaQueryWrapper<DoctorInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DoctorInfo::getTotalAppointments).last("LIMIT 10");
        List<DoctorInfo> doctors = doctorInfoService.list(wrapper);
        for (DoctorInfo doctor : doctors) {
            Map<String, Object> item = new java.util.HashMap<>();
            SysUser user = userService.getById(doctor.getUserId());
            item.put("name", user != null ? user.getRealName() : "未知");
            item.put("count", doctor.getTotalAppointments());
            ranking.add(item);
        }
        return Result.success(ranking);
    }
}
