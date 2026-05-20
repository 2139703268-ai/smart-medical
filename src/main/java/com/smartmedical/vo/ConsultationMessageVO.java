package com.smartmedical.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultationMessageVO {

    private Long id;
    private Long consultationId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private String msgType;
    private String imageUrl;
    private LocalDateTime createTime;
}
