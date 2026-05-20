package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("patient_family")
public class PatientFamily {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String idCard;

    private String phone;

    private String relationship;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
