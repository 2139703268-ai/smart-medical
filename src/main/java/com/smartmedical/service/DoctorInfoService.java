package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.DoctorInfo;
import com.smartmedical.vo.DoctorVO;

import java.util.List;

public interface DoctorInfoService extends IService<DoctorInfo> {

    Page<DoctorVO> getDoctorsByDepartment(Long departmentId, Integer pageNum, Integer pageSize);

    List<DoctorVO> getHotDoctors(Integer limit);

    DoctorVO getDoctorDetail(Long doctorId);
}
