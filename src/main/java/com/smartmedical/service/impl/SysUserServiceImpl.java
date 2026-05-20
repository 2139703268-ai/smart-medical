package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.common.BusinessException;
import com.smartmedical.entity.SysUser;
import com.smartmedical.mapper.SysUserMapper;
import com.smartmedical.service.SysUserService;
import com.smartmedical.utils.JwtUtils;
import com.smartmedical.vo.LoginVO;
import com.smartmedical.vo.RegisterVO;
import com.smartmedical.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public LoginVO register(RegisterVO registerVO) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, registerVO.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerVO.getUsername());
        user.setPassword(passwordEncoder.encode(registerVO.getPassword()));
        user.setRealName(registerVO.getRealName());
        user.setPhone(registerVO.getPhone());
        user.setRole("PATIENT");
        user.setStatus(1);
        this.save(user);

        // 生成token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }

    @Override
    public LoginVO login(String username, String password) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = this.getOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setIdCard(user.getIdCard());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = this.getById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    @Override
    public void updateUserInfo(Long userId, SysUser user) {
        user.setId(userId);
        this.updateById(user);
    }
}
