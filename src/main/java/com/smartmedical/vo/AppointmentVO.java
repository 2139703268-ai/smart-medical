package com.smartmedical.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppointmentVO {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private Long departmentId;
    private String departmentName;
    private Long scheduleId;
    private LocalDateTime appointmentTime;
    private String timeSlot;
    private Integer queueNumber;
    private BigDecimal fee;
    private String status;
    private String cancelReason;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
}
