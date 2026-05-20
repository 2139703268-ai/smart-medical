package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.Schedule;
import com.smartmedical.vo.ScheduleVO;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService extends IService<Schedule> {

    List<ScheduleVO> getSchedulesByDoctor(Long doctorId, LocalDate startDate, LocalDate endDate);

    void addSchedule(Schedule schedule);

    void updateSchedule(Schedule schedule);

    void deleteSchedule(Long id);
}
