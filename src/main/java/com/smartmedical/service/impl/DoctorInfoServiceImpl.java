package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.Department;
import com.smartmedical.entity.DoctorInfo;
import com.smartmedical.entity.SysUser;
import com.smartmedical.mapper.DoctorInfoMapper;
import com.smartmedical.service.DepartmentService;
import com.smartmedical.service.DoctorInfoService;
import com.smartmedical.service.SysUserService;
import com.smartmedical.vo.DoctorVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorInfoServiceImpl extends ServiceImpl<DoctorInfoMapper, DoctorInfo> implements DoctorInfoService {

    private final SysUserService userService;
    private final DepartmentService departmentService;

    @Override
    public Page<DoctorVO> getDoctorsByDepartment(Long departmentId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<DoctorInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorInfo::getDepartmentId, departmentId)
               .orderByDesc(DoctorInfo::getTotalAppointments);
        Page<DoctorInfo> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        Page<DoctorVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<DoctorVO> getHotDoctors(Integer limit) {
        LambdaQueryWrapper<DoctorInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DoctorInfo::getTotalAppointments)
               .last("LIMIT " + limit);
        return this.list(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorVO getDoctorDetail(Long doctorId) {
        DoctorInfo doctor = this.getById(doctorId);
        return convertToVO(doctor);
    }

    private DoctorVO convertToVO(DoctorInfo doctor) {
        DoctorVO vo = new DoctorVO();
        vo.setId(doctor.getId());
        vo.setUserId(doctor.getUserId());
        vo.setDepartmentId(doctor.getDepartmentId());
        vo.setTitle(doctor.getTitle());
        vo.setSpecialty(doctor.getSpecialty());
        vo.setIntroduction(doctor.getIntroduction());
        vo.setRegistrationFee(doctor.getRegistrationFee());
        vo.setTotalAppointments(doctor.getTotalAppointments());
        vo.setRating(doctor.getRating());

        // 获取用户信息
        SysUser user = userService.getById(doctor.getUserId());
        if (user != null) {
            vo.setName(user.getRealName());
            vo.setAvatar(user.getAvatar());
        }

        // 获取科室信息
        Department dept = departmentService.getById(doctor.getDepartmentId());
        if (dept != null) {
            vo.setDepartmentName(dept.getName());
        }

        return vo;
    }
}
