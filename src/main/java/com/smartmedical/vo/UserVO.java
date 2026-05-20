package com.smartmedical.vo;

import lombok.Data;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String idCard;
    private String avatar;
    private String role;
}
