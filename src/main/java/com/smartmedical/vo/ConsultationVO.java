package com.smartmedical.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ConsultationVO {

    private Long id;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private Long departmentId;
    private String departmentName;
    private String type;
    private String status;
    private String symptom;
    private String symptomImages;
    private BigDecimal fee;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String cancelReason;
    private LocalDateTime createTime;
}
