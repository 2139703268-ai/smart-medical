package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.Message;
import com.smartmedical.vo.MessageVO;

public interface MessageService extends IService<Message> {

    void sendMessage(Long userId, String title, String content, String type);

    Page<Message> getUserMessages(Long userId, Integer pageNum, Integer pageSize);

    Page<MessageVO> getUserMessageVOs(Long userId, Integer pageNum, Integer pageSize);

    long getUnreadCount(Long userId);

    void markAsRead(Long messageId);

    void markAllAsRead(Long userId);
}
