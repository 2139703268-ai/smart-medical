package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.ConsultationMessage;
import com.smartmedical.vo.ConsultationMessageVO;

public interface ConsultationMessageService extends IService<ConsultationMessage> {

    ConsultationMessageVO sendMessage(Long consultationId, Long senderId, String senderRole, String content, String msgType, String imageUrl);

    Page<ConsultationMessageVO> getMessages(Long consultationId, Integer pageNum, Integer pageSize);
}
