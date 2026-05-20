package com.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartmedical.entity.Announcement;
import com.smartmedical.mapper.AnnouncementMapper;
import com.smartmedical.service.AnnouncementService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public List<Announcement> getLatestAnnouncements(Integer limit) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1)
               .orderByDesc(Announcement::getTopFlag)
               .orderByDesc(Announcement::getCreateTime)
               .last("LIMIT " + limit);
        return this.list(wrapper);
    }
}
