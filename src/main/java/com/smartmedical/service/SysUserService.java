package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.SysUser;
import com.smartmedical.vo.LoginVO;
import com.smartmedical.vo.RegisterVO;
import com.smartmedical.vo.UserVO;

public interface SysUserService extends IService<SysUser> {

    LoginVO register(RegisterVO registerVO);

    LoginVO login(String username, String password);

    UserVO getCurrentUser(Long userId);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    void updateUserInfo(Long userId, SysUser user);
}
