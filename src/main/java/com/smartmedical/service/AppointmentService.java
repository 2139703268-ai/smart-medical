package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.Appointment;
import com.smartmedical.vo.AppointmentVO;

public interface AppointmentService extends IService<Appointment> {

    AppointmentVO createAppointment(Long patientId, Long scheduleId, Long familyId);

    void cancelAppointment(Long appointmentId, Long userId, String role);

    void completeAppointment(Long appointmentId);

    Page<AppointmentVO> getPatientAppointments(Long patientId, String status, Integer pageNum, Integer pageSize);

    Page<AppointmentVO> getDoctorAppointments(Long doctorId, String status, Integer pageNum, Integer pageSize);

    AppointmentVO getAppointmentDetail(Long appointmentId);

    void autoCancelExpiredAppointments();
}
