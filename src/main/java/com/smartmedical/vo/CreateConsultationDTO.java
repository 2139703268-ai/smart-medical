package com.smartmedical.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateConsultationDTO {

    @NotNull(message = "医生ID不能为空")
    private Long doctorId;

    @NotBlank(message = "问诊类型不能为空")
    private String type;

    @NotBlank(message = "症状描述不能为空")
    private String symptom;

    private String symptomImages;
}
