package com.smartmedical.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;
    private String title;
    private String content;
    private String type;
    private Integer readStatus;
    private LocalDateTime createTime;
}
