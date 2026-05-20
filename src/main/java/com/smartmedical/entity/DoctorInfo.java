package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("doctor_info")
public class DoctorInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long departmentId;

    private String title;

    private String specialty;

    private String introduction;

    private BigDecimal registrationFee;

    private Integer totalAppointments;

    private BigDecimal rating;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
