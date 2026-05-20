package com.smartmedical.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmedical.entity.*;
import com.smartmedical.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final DoctorInfoMapper doctorInfoMapper;
    private final ScheduleMapper scheduleMapper;
    private final AnnouncementMapper announcementMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initDepartments();
        initDoctorInfos();
        initSchedules();
        initAnnouncements();
        log.info("数据初始化完成");
    }

    private void initUsers() {
        createUserIfNotExists("admin", "admin123", "系统管理员", null, "ADMIN");
        createUserIfNotExists("doctor1", "doctor123", "张医生", "13800000001", "DOCTOR");
        createUserIfNotExists("doctor2", "doctor123", "李医生", "13800000002", "DOCTOR");
        createUserIfNotExists("doctor3", "doctor123", "王医生", "13800000003", "DOCTOR");
        createUserIfNotExists("patient1", "patient123", "张三", "13900000001", "PATIENT");
        createUserIfNotExists("patient2", "patient123", "李四", "13900000002", "PATIENT");
        createUserIfNotExists("pharmacist1", "pharmacist123", "赵药师", "13800000010", "PHARMACIST");
    }

    private void createUserIfNotExists(String username, String password, String realName, String phone, String role) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        if (userMapper.selectCount(wrapper) > 0) {
            return;
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);
        log.info("创建测试账号: {} ({})", username, role);
    }

    private void initDepartments() {
        if (departmentMapper.selectCount(null) > 0) return;
        String[][] depts = {
            {"内科", "内科是临床医学的一个专科，主要诊治心血管、呼吸、消化、泌尿、内分泌等系统的疾病。"},
            {"外科", "外科是临床医学的一个专科，主要通过手术方式诊治疾病。"},
            {"儿科", "儿科是专门研究儿童疾病诊治的医学专科。"},
            {"妇产科", "妇产科是专门研究女性生殖系统疾病及妊娠分娩的医学专科。"},
            {"骨科", "骨科是专门研究骨骼、关节、肌肉等运动系统疾病的医学专科。"},
            {"眼科", "眼科是专门研究眼部疾病诊治的医学专科。"},
            {"耳鼻喉科", "耳鼻喉科是专门研究耳、鼻、咽喉疾病诊治的医学专科。"},
            {"口腔科", "口腔科是专门研究口腔疾病诊治的医学专科。"},
            {"皮肤科", "皮肤科是专门研究皮肤疾病诊治的医学专科。"},
            {"中医科", "中医科是运用中医理论和方法诊治疾病的医学专科。"}
        };
        for (int i = 0; i < depts.length; i++) {
            Department dept = new Department();
            dept.setName(depts[i][0]);
            dept.setDescription(depts[i][1]);
            dept.setSort(i + 1);
            dept.setStatus(1);
            dept.setDeleted(0);
            departmentMapper.insert(dept);
        }
        log.info("创建测试科室: {}个", depts.length);
    }

    private void initDoctorInfos() {
        if (doctorInfoMapper.selectCount(null) > 0) return;
        Long doctor1Id = getUserId("doctor1");
        Long doctor2Id = getUserId("doctor2");
        Long doctor3Id = getUserId("doctor3");
        if (doctor1Id == null || doctor2Id == null || doctor3Id == null) return;

        Long dept1Id = getDeptId("内科");
        Long dept2Id = getDeptId("外科");
        Long dept3Id = getDeptId("儿科");
        if (dept1Id == null || dept2Id == null || dept3Id == null) return;

        createDoctorInfo(doctor1Id, dept1Id, "主任医师", "心血管疾病、高血压、冠心病",
                "从医20余年，擅长心血管疾病的诊治，具有丰富的临床经验。", 50.00);
        createDoctorInfo(doctor2Id, dept2Id, "副主任医师", "骨科手术、关节置换、创伤治疗",
                "骨科专家，擅长各类骨科手术，成功完成手术数千例。", 40.00);
        createDoctorInfo(doctor3Id, dept3Id, "主治医师", "小儿感冒、发热、咳嗽",
                "儿科医生，对小儿常见病有丰富的诊疗经验。", 30.00);
        log.info("创建医生信息: 3条");
    }

    private void createDoctorInfo(Long userId, Long deptId, String title, String specialty, String intro, double fee) {
        DoctorInfo info = new DoctorInfo();
        info.setUserId(userId);
        info.setDepartmentId(deptId);
        info.setTitle(title);
        info.setSpecialty(specialty);
        info.setIntroduction(intro);
        info.setRegistrationFee(BigDecimal.valueOf(fee));
        info.setTotalAppointments(0);
        info.setRating(BigDecimal.valueOf(5.0));
        info.setDeleted(0);
        doctorInfoMapper.insert(info);
    }

    private void initSchedules() {
        Long doctor1Id = getDoctorInfoId("doctor1");
        Long doctor2Id = getDoctorInfoId("doctor2");
        Long doctor3Id = getDoctorInfoId("doctor3");
        if (doctor1Id == null || doctor2Id == null || doctor3Id == null) return;

        Long dept1Id = getDeptId("内科");
        Long dept2Id = getDeptId("外科");
        Long dept3Id = getDeptId("儿科");

        int count = 0;
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            count += createSchedule(doctor1Id, dept1Id, date, "MORNING", 20);
            count += createSchedule(doctor1Id, dept1Id, date, "AFTERNOON", 15);
            count += createSchedule(doctor2Id, dept2Id, date, "MORNING", 15);
            count += createSchedule(doctor2Id, dept2Id, date, "AFTERNOON", 10);
            count += createSchedule(doctor3Id, dept3Id, date, "MORNING", 25);
            count += createSchedule(doctor3Id, dept3Id, date, "AFTERNOON", 20);
        }
        if (count > 0) log.info("创建排班数据: {}条", count);
    }

    private int createSchedule(Long doctorId, Long deptId, LocalDate date, String slot, int total) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId)
               .eq(Schedule::getScheduleDate, date)
               .eq(Schedule::getTimeSlot, slot);
        if (scheduleMapper.selectCount(wrapper) > 0) return 0;

        Schedule s = new Schedule();
        s.setDoctorId(doctorId);
        s.setDepartmentId(deptId);
        s.setScheduleDate(date);
        s.setTimeSlot(slot);
        s.setTotalNumber(total);
        s.setRemainingNumber(total);
        s.setStatus(1);
        s.setDeleted(0);
        scheduleMapper.insert(s);
        return 1;
    }

    private void initAnnouncements() {
        if (announcementMapper.selectCount(null) > 0) return;
        createAnnouncement("医院开诊通知", "本院已全面恢复正常诊疗，请各位患者合理安排就诊时间。", "ALL", 1);
        createAnnouncement("关于预约挂号的说明", "患者可通过本平台提前7天预约挂号，预约成功后请按时就诊。", "PATIENT", 0);
        createAnnouncement("医生排班调整通知", "本周五下午外科停诊，请需要就诊的患者选择其他时间。", "ALL", 0);
        log.info("创建公告: 3条");
    }

    private void createAnnouncement(String title, String content, String targetRole, int topFlag) {
        Announcement a = new Announcement();
        a.setTitle(title);
        a.setContent(content);
        a.setTargetRole(targetRole);
        a.setTopFlag(topFlag);
        a.setStatus(1);
        a.setDeleted(0);
        announcementMapper.insert(a);
    }

    private Long getUserId(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = userMapper.selectOne(wrapper);
        return user != null ? user.getId() : null;
    }

    private Long getDeptId(String name) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getName, name);
        Department dept = departmentMapper.selectOne(wrapper);
        return dept != null ? dept.getId() : null;
    }

    private Long getDoctorInfoId(String username) {
        Long userId = getUserId(username);
        if (userId == null) return null;
        LambdaQueryWrapper<DoctorInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorInfo::getUserId, userId);
        DoctorInfo info = doctorInfoMapper.selectOne(wrapper);
        return info != null ? info.getId() : null;
    }
}
