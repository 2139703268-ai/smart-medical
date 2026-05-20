package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("medical_record")
public class MedicalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long appointmentId;

    private Long patientId;

    private Long doctorId;

    private String diagnosis;

    private String prescription;

    private String advice;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
