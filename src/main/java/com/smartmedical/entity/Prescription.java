package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prescription")
public class Prescription {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long consultationId;

    private Long appointmentId;

    private Long patientId;

    private Long doctorId;

    private String diagnosis;

    private String content;

    private String advice;

    private String status;

    private Long pharmacistId;

    private LocalDateTime reviewTime;

    private String rejectReason;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
