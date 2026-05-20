# 智慧医疗预约服务平台

> 基于 Vue3 + SpringBoot 的智慧医疗预约服务平台

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.x-brightgreen.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](#许可证)

---

## 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [测试账号](#测试账号)
- [功能说明](#功能说明)
- [技术亮点](#技术亮点)
- [API文档](#api文档)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

---

## 项目简介

智慧医疗预约服务平台是一个现代化的医疗预约管理系统，旨在简化患者预约流程、提高医院管理效率。平台支持三种用户角色：患者、医生和管理员，提供完整的预约挂号、排班管理、数据统计等功能。

### 核心特性

- 多角色支持：患者、医生、管理员
- 实时预约：智能排班与号源管理
- 数据可视化：直观的统计大屏
- 安全认证：JWT + Spring Security
- 响应式设计：适配多种设备

---

## 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2 | 应用框架 |
| MyBatis-Plus | 3.5 | ORM框架 |
| Spring Security | - | 安全框架 |
| JWT | - | 身份认证 |
| MySQL | 8.x | 数据库 |
| Maven | 3.6+ | 项目管理 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.x | 前端框架 |
| Element Plus | - | UI组件库 |
| Vue Router | - | 路由管理 |
| Pinia | - | 状态管理 |
| Axios | - | HTTP客户端 |
| ECharts | - | 数据可视化 |

---

## 项目结构

```
smart-medical/
├── smart-medical-backend/          # 后端SpringBoot项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/              # Java源代码
│   │   │   └── resources/         # 配置文件
│   │   └── test/                  # 测试代码
│   ├── pom.xml                    # Maven配置
│   └── ...
│
├── smart-medical-frontend/         # 前端Vue项目
│   ├── src/
│   │   ├── api/                   # API接口
│   │   ├── components/            # 组件
│   │   ├── router/                # 路由配置
│   │   ├── stores/                # Pinia状态
│   │   ├── views/                 # 页面视图
│   │   └── App.vue                # 主组件
│   ├── package.json               # 依赖配置
│   └── ...
│
├── sql/                            # 数据库脚本
│   └── init.sql                   # 初始化脚本
│
└── README.md                       # 项目说明
```

---

## 快速开始

### 环境要求

- **JDK**: 17 或更高版本
- **Node.js**: 16 或更高版本
- **MySQL**: 8.x
- **Maven**: 3.6+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/smart-medical.git
cd smart-medical
```

### 2. 初始化数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source sql/init.sql
```

或者直接命令行执行：

```bash
mysql -u root -p < sql/init.sql
```

### 3. 配置后端

编辑 `smart-medical-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_medical?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  secret: your-jwt-secret-key
  expiration: 86400000  # 24小时
```

### 4. 启动后端服务

```bash
cd smart-medical-backend

# 使用Maven启动
mvn spring-boot:run

# 或者打包后运行
mvn clean package -DskipTests
java -jar target/smart-medical-backend-*.jar
```

后端服务将在 http://localhost:8080 启动

### 5. 启动前端服务

```bash
cd smart-medical-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端应用将在 http://localhost:5173 启动

---

## 测试账号

系统预置了以下测试账号，可直接登录体验：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 拥有系统管理权限 |
| 医生 | doctor1 | doctor123 | 可管理排班和预约 |
| 患者 | patient1 | patient123 | 可进行预约挂号 |

---

## 功能说明

### 患者端功能

#### 首页
- 科室导航：快速浏览各科室信息
- 热门医生：展示推荐医生列表
- 公告轮播：最新医院公告

#### 预约挂号
```
选择科室 → 选择医生 → 选择时间 → 确认预约 → 支付
```

#### 我的预约
- 查看预约记录
- 取消未就诊预约
- 查看就诊状态

#### 消息通知
- 预约提醒
- 系统公告
- 就诊通知

#### 个人中心
- 修改个人信息
- 修改密码
- 就诊人管理

---

### 医生端功能

#### 工作台
- 今日预约概览
- 就诊统计数据
- 快捷操作入口

#### 排班管理
- 日历视图展示
- 添加/修改/删除排班
- 批量排班设置

#### 预约列表
- 查看待就诊预约
- 完成就诊记录
- 取消异常预约

#### 病历填写
- 诊断信息录入
- 处方开具
- 医嘱说明

---

### 管理员端功能

#### 数据大屏
- 核心指标展示
- 实时数据统计
- 图表可视化

#### 科室管理
- 科室信息维护
- 科室增删改查

#### 医生管理
- 医生账号管理
- 医生信息维护
- 权限分配

#### 患者管理
- 患者列表查看
- 黑名单管理
- 患者信息导出

#### 公告管理
- 发布新公告
- 编辑公告内容
- 删除过期公告

---

## 技术亮点

### 1. 并发控制
采用乐观锁机制确保号源并发安全：
```sql
UPDATE schedule 
SET available_slots = available_slots - 1, version = version + 1
WHERE id = ? AND version = ? AND available_slots > 0
```

### 2. 定时任务
自动取消超时未支付预约：
```java
@Scheduled(fixedRate = 60000)  // 每分钟执行
public void cancelUnpaidAppointments() {
    // 取消超过30分钟未支付的预约
}
```

### 3. 数据可视化
使用ECharts实现丰富的图表展示：
- 预约趋势图
- 科室分布图
- 医生工作量统计
- 收入统计报表

### 4. 安全认证
- JWT无状态认证
- Spring Security权限控制
- 接口级别权限管理
- 密码BCrypt加密

---

## API文档

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/logout | 用户登出 |

### 患者接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/departments | 获取科室列表 |
| GET | /api/doctors | 获取医生列表 |
| POST | /api/appointments | 创建预约 |
| GET | /api/appointments | 获取预约列表 |
| PUT | /api/appointments/{id}/cancel | 取消预约 |

### 医生接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/doctor/schedules | 获取排班列表 |
| POST | /api/doctor/schedules | 添加排班 |
| GET | /api/doctor/appointments | 获取预约列表 |
| PUT | /api/doctor/appointments/{id}/complete | 完成就诊 |

### 管理员接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/statistics | 获取统计数据 |
| CRUD | /api/admin/departments | 科室管理 |
| CRUD | /api/admin/doctors | 医生管理 |
| CRUD | /api/admin/patients | 患者管理 |
| CRUD | /api/admin/announcements | 公告管理 |

---

## 常见问题

### Q: 启动后端时数据库连接失败？
A: 请检查：
1. MySQL服务是否启动
2. `application.yml`中的数据库配置是否正确
3. 数据库用户是否有足够权限

### Q: 前端启动后页面空白？
A: 请检查：
1. 后端服务是否正常运行
2. 浏览器控制台是否有错误信息
3. 网络请求是否正常

### Q: 如何修改端口号？
A: 
- 后端：修改 `application.yml` 中的 `server.port`
- 前端：修改 `vite.config.js` 中的 `server.port`

---

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'Add some feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

### 代码规范

- 后端遵循阿里巴巴Java开发手册
- 前端遵循Vue.js风格指南
- 提交信息使用中文，格式：`[类型] 描述`

---

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue
- 发送邮件至：your-email@example.com

---

**感谢使用智慧医疗预约服务平台！**
