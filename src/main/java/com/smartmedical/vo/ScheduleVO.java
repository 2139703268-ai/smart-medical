package com.smartmedical.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleVO {

    private Long id;
    private Long doctorId;
    private Long departmentId;
    private LocalDate scheduleDate;
    private String timeSlot;
    private Integer totalNumber;
    private Integer remainingNumber;
    private Integer status;
}
