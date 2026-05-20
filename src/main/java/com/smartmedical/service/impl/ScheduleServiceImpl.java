package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.common.BusinessException;
import com.smartmedical.entity.Schedule;
import com.smartmedical.mapper.ScheduleMapper;
import com.smartmedical.service.ScheduleService;
import com.smartmedical.vo.ScheduleVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {

    @Override
    public List<ScheduleVO> getSchedulesByDoctor(Long doctorId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, doctorId)
               .ge(Schedule::getScheduleDate, startDate)
               .le(Schedule::getScheduleDate, endDate)
               .orderByAsc(Schedule::getScheduleDate);
        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void addSchedule(Schedule schedule) {
        // 检查是否已存在该日期和时段的排班
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDoctorId, schedule.getDoctorId())
               .eq(Schedule::getScheduleDate, schedule.getScheduleDate())
               .eq(Schedule::getTimeSlot, schedule.getTimeSlot());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("该时段已存在排班");
        }
        schedule.setRemainingNumber(schedule.getTotalNumber());
        schedule.setStatus(1);
        this.save(schedule);
    }

    @Override
    public void updateSchedule(Schedule schedule) {
        this.updateById(schedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        this.removeById(id);
    }

    private ScheduleVO convertToVO(Schedule schedule) {
        ScheduleVO vo = new ScheduleVO();
        vo.setId(schedule.getId());
        vo.setDoctorId(schedule.getDoctorId());
        vo.setDepartmentId(schedule.getDepartmentId());
        vo.setScheduleDate(schedule.getScheduleDate());
        vo.setTimeSlot(schedule.getTimeSlot());
        vo.setTotalNumber(schedule.getTotalNumber());
        vo.setRemainingNumber(schedule.getRemainingNumber());
        vo.setStatus(schedule.getStatus());
        return vo;
    }
}
