package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.Evaluation;
import com.smartmedical.mapper.EvaluationMapper;
import com.smartmedical.service.EvaluationService;
import org.springframework.stereotype.Service;

@Service
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    @Override
    public void saveEvaluation(Evaluation evaluation) {
        this.save(evaluation);
    }

    @Override
    public Evaluation getByAppointmentId(Long appointmentId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getAppointmentId, appointmentId);
        return this.getOne(wrapper);
    }
}
