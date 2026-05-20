package com.smartmedical.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmedical.entity.DoctorInfo;
import com.smartmedical.entity.Schedule;
import com.smartmedical.mapper.DoctorInfoMapper;
import com.smartmedical.mapper.ScheduleMapper;
import com.smartmedical.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleTask {

    private final AppointmentService appointmentService;
    private final DoctorInfoMapper doctorInfoMapper;
    private final ScheduleMapper scheduleMapper;

    // 每5分钟执行一次，自动取消超过15分钟未支付的预约
    @Scheduled(fixedRate = 300000)
    public void autoCancelExpiredAppointments() {
        log.info("开始执行自动取消过期预约任务...");
        appointmentService.autoCancelExpiredAppointments();
        log.info("自动取消过期预约任务完成");
    }

    // 每天凌晨1点自动生成未来7天的排班
    @Scheduled(cron = "0 0 1 * * ?")
    public void autoGenerateSchedules() {
        log.info("开始自动生成排班数据...");
        LocalDate targetDate = LocalDate.now().plusDays(7);
        var doctors = doctorInfoMapper.selectList(null);
        int count = 0;
        for (DoctorInfo doctor : doctors) {
            count += createIfNotExists(doctor.getId(), doctor.getDepartmentId(), targetDate, "MORNING", 20);
            count += createIfNotExists(doctor.getId(), doctor.getDepartmentId(), targetDate, "AFTERNOON", 15);
        }
        log.info("自动生成排班完成，新增{}条", count);
    }

    private int createIfNotExists(Long doctorId, Long deptId, LocalDate date, String slot, int total) {
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
}
