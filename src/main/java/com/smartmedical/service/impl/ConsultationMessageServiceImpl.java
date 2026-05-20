package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.common.BusinessException;
import com.smartmedical.entity.ConsultationMessage;
import com.smartmedical.entity.DoctorInfo;
import com.smartmedical.entity.OnlineConsultation;
import com.smartmedical.entity.SysUser;
import com.smartmedical.mapper.ConsultationMessageMapper;
import com.smartmedical.service.ConsultationMessageService;
import com.smartmedical.service.DoctorInfoService;
import com.smartmedical.service.OnlineConsultationService;
import com.smartmedical.service.SysUserService;
import com.smartmedical.vo.ConsultationMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationMessageServiceImpl extends ServiceImpl<ConsultationMessageMapper, ConsultationMessage> implements ConsultationMessageService {

    private final OnlineConsultationService consultationService;
    private final SysUserService userService;
    private final DoctorInfoService doctorInfoService;

    @Override
    public ConsultationMessageVO sendMessage(Long consultationId, Long senderId, String senderRole, String content, String msgType, String imageUrl) {
        OnlineConsultation consultation = consultationService.getById(consultationId);
        if (consultation == null) {
            throw new BusinessException("问诊不存在");
        }
        if (!"IN_PROGRESS".equals(consultation.getStatus())) {
            throw new BusinessException("问诊未开始或已结束");
        }

        // 校验发送者是否属于此问诊
        if ("PATIENT".equals(senderRole) && !consultation.getPatientId().equals(senderId)) {
            throw new BusinessException("无权发送消息");
        }
        if ("DOCTOR".equals(senderRole) && !consultation.getDoctorId().equals(senderId)) {
            throw new BusinessException("无权发送消息");
        }

        ConsultationMessage message = new ConsultationMessage();
        message.setConsultationId(consultationId);
        message.setSenderId(senderId);
        message.setSenderRole(senderRole);
        message.setContent(content);
        message.setMsgType(msgType);
        message.setImageUrl(imageUrl);
        this.save(message);

        return convertToVO(message);
    }

    @Override
    public Page<ConsultationMessageVO> getMessages(Long consultationId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ConsultationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationMessage::getConsultationId, consultationId)
               .orderByAsc(ConsultationMessage::getCreateTime);
        Page<ConsultationMessage> page = this.page(new Page<>(pageNum, pageSize), wrapper);

        Page<ConsultationMessageVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    private ConsultationMessageVO convertToVO(ConsultationMessage m) {
        ConsultationMessageVO vo = new ConsultationMessageVO();
        vo.setId(m.getId());
        vo.setConsultationId(m.getConsultationId());
        vo.setSenderId(m.getSenderId());
        vo.setSenderRole(m.getSenderRole());
        vo.setContent(m.getContent());
        vo.setMsgType(m.getMsgType());
        vo.setImageUrl(m.getImageUrl());
        vo.setCreateTime(m.getCreateTime());

        if ("DOCTOR".equals(m.getSenderRole())) {
            DoctorInfo doctor = doctorInfoService.getById(m.getSenderId());
            if (doctor != null) {
                SysUser doctorUser = userService.getById(doctor.getUserId());
                if (doctorUser != null) {
                    vo.setSenderName(doctorUser.getRealName());
                }
            }
        } else {
            SysUser sender = userService.getById(m.getSenderId());
            if (sender != null) {
                vo.setSenderName(sender.getRealName());
            }
        }

        return vo;
    }
}
