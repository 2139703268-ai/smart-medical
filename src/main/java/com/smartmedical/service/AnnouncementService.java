package com.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartmedical.entity.Announcement;

import java.util.List;

public interface AnnouncementService extends IService<Announcement> {

    List<Announcement> getLatestAnnouncements(Integer limit);
}
