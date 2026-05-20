package com.smartmedical.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("consultation_message")
public class ConsultationMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long consultationId;

    private Long senderId;

    private String senderRole;

    private String content;

    private String msgType;

    private String imageUrl;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
