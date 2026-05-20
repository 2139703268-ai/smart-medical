package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;

    private Long doctorId;

    private Long departmentId;

    private Long scheduleId;

    private LocalDateTime appointmentTime;

    private String timeSlot;

    private Integer queueNumber;

    private BigDecimal fee;

    private String status;

    private String cancelReason;

    private LocalDateTime payTime;

    private LocalDateTime cancelTime;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
