package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.Message;
import com.smartmedical.mapper.MessageMapper;
import com.smartmedical.service.MessageService;
import com.smartmedical.vo.MessageVO;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public void sendMessage(Long userId, String title, String content, String type) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setReadStatus(0);
        this.save(message);
    }

    @Override
    public Page<Message> getUserMessages(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId)
               .orderByDesc(Message::getCreateTime);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void markAsRead(Long messageId) {
        Message message = this.getById(messageId);
        if (message != null) {
            message.setReadStatus(1);
            this.updateById(message);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Message> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Message::getUserId, userId)
               .eq(Message::getReadStatus, 0)
               .set(Message::getReadStatus, 1);
        this.update(wrapper);
    }

    @Override
    public Page<MessageVO> getUserMessageVOs(Long userId, Integer pageNum, Integer pageSize) {
        Page<Message> page = getUserMessages(userId, pageNum, pageSize);
        Page<MessageVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId)
               .eq(Message::getReadStatus, 0);
        return this.count(wrapper);
    }

    private MessageVO convertToVO(Message message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setReadStatus(message.getReadStatus());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }
}
