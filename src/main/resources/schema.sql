-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    id_card VARCHAR(20) COMMENT '身份证号',
    avatar VARCHAR(255) COMMENT '头像',
    role VARCHAR(20) NOT NULL DEFAULT 'PATIENT' COMMENT '角色: PATIENT/DOCTOR/ADMIN/PHARMACIST',
    status INT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 科室表
CREATE TABLE IF NOT EXISTS department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '科室名称',
    description TEXT COMMENT '科室描述',
    icon VARCHAR(255) COMMENT '科室图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status INT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

-- 医生信息表
CREATE TABLE IF NOT EXISTS doctor_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    department_id BIGINT NOT NULL COMMENT '科室ID',
    title VARCHAR(50) COMMENT '职称',
    specialty VARCHAR(200) COMMENT '擅长',
    introduction TEXT COMMENT '个人简介',
    registration_fee DECIMAL(10,2) DEFAULT 0 COMMENT '挂号费',
    total_appointments INT DEFAULT 0 COMMENT '总预约量',
    rating DECIMAL(3,1) DEFAULT 5.0 COMMENT '评分',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生信息表';

-- 排班表
CREATE TABLE IF NOT EXISTS schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    department_id BIGINT NOT NULL COMMENT '科室ID',
    schedule_date DATE NOT NULL COMMENT '排班日期',
    time_slot VARCHAR(20) NOT NULL COMMENT '时段: MORNING/AFTERNOON',
    total_number INT NOT NULL DEFAULT 0 COMMENT '总号源数',
    remaining_number INT NOT NULL DEFAULT 0 COMMENT '剩余号源数',
    status INT NOT NULL DEFAULT 1 COMMENT '状态: 0-停诊 1-正常',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_schedule_date (schedule_date),
    UNIQUE INDEX uk_doctor_date_slot (doctor_id, schedule_date, time_slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班表';

-- 预约表
CREATE TABLE IF NOT EXISTS appointment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    department_id BIGINT NOT NULL COMMENT '科室ID',
    schedule_id BIGINT NOT NULL COMMENT '排班ID',
    appointment_time DATETIME COMMENT '预约时间',
    time_slot VARCHAR(20) COMMENT '时段',
    queue_number INT COMMENT '排队序号',
    fee DECIMAL(10,2) DEFAULT 0 COMMENT '挂号费',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PAID/COMPLETED/CANCELLED',
    cancel_reason VARCHAR(200) COMMENT '取消原因',
    pay_time DATETIME COMMENT '支付时间',
    cancel_time DATETIME COMMENT '取消时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- 病历表
CREATE TABLE IF NOT EXISTS medical_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL COMMENT '预约ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    diagnosis TEXT COMMENT '诊断结果',
    prescription TEXT COMMENT '处方',
    advice TEXT COMMENT '医嘱',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_patient_id (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历表';

-- 评价表
CREATE TABLE IF NOT EXISTS evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    appointment_id BIGINT NOT NULL COMMENT '预约ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    rating INT NOT NULL COMMENT '评分: 1-5',
    content TEXT COMMENT '评价内容',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_doctor_id (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 公告表
CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    target_role VARCHAR(20) DEFAULT 'ALL' COMMENT '目标角色: ALL/PATIENT/DOCTOR',
    top_flag INT DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
    status INT NOT NULL DEFAULT 1 COMMENT '状态: 0-草稿 1-发布',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 消息表
CREATE TABLE IF NOT EXISTS message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(100) COMMENT '标题',
    content TEXT COMMENT '内容',
    type VARCHAR(20) COMMENT '类型: APPOINTMENT/SYSTEM',
    read_status INT NOT NULL DEFAULT 0 COMMENT '已读状态: 0-未读 1-已读',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_read_status (read_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 就诊人表
CREATE TABLE IF NOT EXISTS patient_family (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    id_card VARCHAR(20) COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '手机号',
    relationship VARCHAR(20) COMMENT '与本人关系',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊人表';

-- 问诊会话表
CREATE TABLE IF NOT EXISTS online_consultation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    type VARCHAR(10) NOT NULL COMMENT '问诊类型：TEXT-图文，VIDEO-视频',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/IN_PROGRESS/COMPLETED/CANCELLED',
    symptom TEXT COMMENT '症状描述',
    symptom_images TEXT COMMENT '症状图片JSON数组',
    fee DECIMAL(10,2) DEFAULT 0.00 COMMENT '问诊费',
    room_id VARCHAR(64) COMMENT '视频房间ID',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    cancel_reason VARCHAR(255) COMMENT '取消原因',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊会话表';

-- 问诊消息表
CREATE TABLE IF NOT EXISTS consultation_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultation_id BIGINT NOT NULL COMMENT '问诊ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    sender_role VARCHAR(10) NOT NULL COMMENT '发送者角色：PATIENT/DOCTOR',
    content TEXT COMMENT '消息内容',
    msg_type VARCHAR(10) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型：TEXT/IMAGE/VIDEO_CALL',
    image_url VARCHAR(500) COMMENT '图片URL',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_consultation_id (consultation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊消息表';

-- 电子处方表
CREATE TABLE IF NOT EXISTS prescription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consultation_id BIGINT COMMENT '问诊ID',
    appointment_id BIGINT COMMENT '预约ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    diagnosis VARCHAR(500) COMMENT '诊断结果',
    content TEXT COMMENT '处方内容JSON',
    advice TEXT COMMENT '医嘱',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '状态：PENDING_REVIEW/APPROVED/REJECTED',
    pharmacist_id BIGINT COMMENT '审核药师ID',
    review_time DATETIME COMMENT '审核时间',
    reject_reason VARCHAR(500) COMMENT '驳回原因',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_consultation_id (consultation_id),
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子处方表';

-- 自动更新：添加 appointment_id 字段
ALTER TABLE prescription ADD COLUMN appointment_id BIGINT COMMENT '预约ID' AFTER consultation_id;
ALTER TABLE prescription MODIFY COLUMN consultation_id BIGINT COMMENT '问诊ID';
