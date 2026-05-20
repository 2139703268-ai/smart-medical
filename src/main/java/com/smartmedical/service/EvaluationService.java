package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.Evaluation;

public interface EvaluationService extends IService<Evaluation> {

    void saveEvaluation(Evaluation evaluation);

    Evaluation getByAppointmentId(Long appointmentId);
}
