package com.smartmedical.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorVO {

    private Long id;
    private Long userId;
    private Long departmentId;
    private String name;
    private String avatar;
    private String departmentName;
    private String title;
    private String specialty;
    private String introduction;
    private BigDecimal registrationFee;
    private Integer totalAppointments;
    private BigDecimal rating;
}
