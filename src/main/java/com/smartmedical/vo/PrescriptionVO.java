package com.smartmedical.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrescriptionVO {

    private Long id;
    private Long consultationId;
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private String diagnosis;
    private String content;
    private String advice;
    private String status;
    private Long pharmacistId;
    private String pharmacistName;
    private LocalDateTime reviewTime;
    private String rejectReason;
    private LocalDateTime createTime;
}
