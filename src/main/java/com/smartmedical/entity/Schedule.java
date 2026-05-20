package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("schedule")
public class Schedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long doctorId;

    private Long departmentId;

    private LocalDate scheduleDate;

    private String timeSlot;

    private Integer totalNumber;

    private Integer remainingNumber;

    private Integer status;

    @Version
    private Integer version;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
