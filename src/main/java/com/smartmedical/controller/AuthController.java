package com.smartmedical.controller;

import com.smartmedical.common.Result;
import com.smartmedical.service.SysUserService;
import com.smartmedical.vo.LoginDTO;
import com.smartmedical.vo.LoginVO;
import com.smartmedical.vo.RegisterVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService userService;

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterVO registerVO) {
        return Result.success(userService.register(registerVO));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO.getUsername(), loginDTO.getPassword()));
    }
}
