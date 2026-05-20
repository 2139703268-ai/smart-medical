package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("online_consultation")
public class OnlineConsultation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patientId;

    private Long doctorId;

    private String type;

    private String status;

    private String symptom;

    private String symptomImages;

    private BigDecimal fee;

    private String roomId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String cancelReason;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
