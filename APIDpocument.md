# 项目管理与过程改进课程 API 文档（v1.0）

> 与《项目计划.md》、数据库设计、OpenAPI (`API.json`) 保持一致。遵循 RESTful 风格，统一响应格式。  
> **单老师、单班级场景**，对接学校SSO登录。

## 1. 全局信息
- **Base URL**：`https://{host}/api/v1`
- **认证方式**：JWT（`Authorization: Bearer <token>`）
- **数据格式**：`application/json; charset=utf-8`
- **时间格式**：ISO 8601（UTC+8），示例 `2025-01-01T10:00:00+08:00`
- **通用响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2025-11-17T12:00:00+08:00",
  "traceId": "9b810..."
}
```

| code | 说明 |
| --- | --- |
| 200 | 成功 |
| 400 | 参数错误/业务校验失败 |
| 401 | 未登录或 token 失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 冲突（重复提交、状态不允许） |
| 500 | 系统异常 |

## 2. 模块与迭代范围
| 模块 | 迭代 1 | 迭代 2 |
| --- | --- | --- |
| 认证/用户 | SSO登录、个人资料 | 角色管理 |
| 课程 | 课程信息（单课程） | 公告 |
| 作业 | 作业列表/提交/批改 | 权重、统计 |
| 出勤 | 签到、记录查询 | 请假审批、报表 |
| 讨论区 | （—） | 讨论帖、评论 |
| 通知 | 作业/考勤提醒 | 自定义通知、公告推送 |
| 统计 | 基础学习统计 | 教师 Dashboard |

以下详细列出主要接口（仅展示关键字段，完整结构见 `API.json`）。

---

## 3. 认证与用户

### 3.1 SSO登录流程
1. **前端跳转SSO**：`GET /auth/sso/login`
   - 返回SSO登录URL：`https://ids6.usst.edu.cn/authserver/login?service={回调URL}`
   - 前端重定向到该URL

2. **SSO回调处理**：`GET /auth/sso/callback?ticket=xxx`
   - 后端验证ticket，从SSO获取用户信息（学号、姓名等）
   - 查询/创建本地用户记录
   - 生成JWT token并返回前端（可重定向到前端页面并携带token）

3. **本地登录（备用）**：`POST /auth/login`
   - 请求：`{ "studentNo": "2021001", "password": "xxx" }`
   - 响应：`token`、`refreshToken`、用户角色

### 3.2 刷新 Token
- `POST /auth/refresh`
- 请求：`{ "refreshToken": "xxx" }`

### 3.3 用户详情
- `GET /users/me`
- 返回：基本信息、角色（STUDENT/TEACHER）

### 3.4 更新资料
- `PUT /users/me`
- 字段：`name`、`avatarUrl`、`phone`

---

## 4. 课程（单课程场景）

### 4.1 获取课程信息
- `GET /course`
- 返回：课程基本信息、教师信息、公告摘要（迭代2）

### 4.2 公告（迭代2）
- `GET /course/announcements`：公告列表
- `POST /course/announcements`：教师发布公告（需教师权限）
- 字段：`title`、`content`、`visibleFrom`、`visibleTo`

---

## 5. 作业与成绩

### 5.1 作业列表
- `GET /assignments`
- 过滤：`status`（ONGOING/ENDED）、`type`
- 返回：作业列表（所有学生可见，教师可查看全部）

### 5.2 作业详情
- `GET /assignments/{id}`
- 返回：题目描述、附件、提交规则、我的提交状态（如有）

### 5.3 提交作业
- `POST /assignments/{id}/submissions`
```json
{
  "content": "文字/JSON",
  "attachmentIds": [12, 13]
}
```

### 5.4 查看我的提交
- `GET /assignments/{id}/submissions/me`
- 返回：提交内容、附件、状态、成绩（如已批改）

### 5.5 教师批改
- `POST /assignments/{id}/submissions/{submissionId}/grade`（需教师权限）
```json
{
  "score": 92,
  "feedback": "分析充分，注意格式。",
  "released": true
}
```

### 5.6 成绩查询
- 学生：`GET /grades/me`（我的所有成绩）
- 教师：`GET /grades?assignmentId=...`（查看某作业所有学生成绩）

---

## 6. 出勤与请假

### 6.1 创建考勤（教师）
- `POST /attendance-sessions`（需教师权限）
```json
{
  "title": "第 5 周",
  "mode": "CODE",
  "startAt": "2025-01-01T08:00:00+08:00",
  "endAt": "2025-01-01T10:00:00+08:00",
  "code": "873214"
}
```

### 6.2 签到/签退
- `POST /attendance-sessions/{sessionId}/checkin`
- 请求：`{ "code": "873214", "location": "教学楼A101" }`
- `POST /attendance-sessions/{sessionId}/checkout`

### 6.3 记录查询
- 学生：`GET /attendance-sessions/my`（我的出勤记录）
- 教师：`GET /attendance-sessions/{sessionId}/records`（查看某次考勤所有学生记录）

### 6.4 请假（迭代2）
- `POST /attendance-sessions/{sessionId}/leaves`
- 请求：`{ "reason": "病假", "attachmentId": 123 }`
- 审批：`PUT /leaves/{leaveId}`（教师审批）

---

## 7. 讨论区与互动（迭代2）

### 7.1 帖子
- `GET /discussions`：帖子列表
- `POST /discussions`：发帖
- 字段：`title`、`content`（支持 Markdown）、`attachmentIds`

### 7.2 评论
- `POST /discussions/{id}/comments`
- `GET /discussions/{id}/comments`
- 支持回复：`{ "content": "...", "parentId": 123 }`

### 7.3 点赞/置顶
- `POST /discussions/{id}/like`
- 教师置顶：`PUT /discussions/{id}/pin`（需教师权限）

---

## 8. 通知与消息

### 8.1 获取通知
- `GET /notifications`
- 过滤：`type`（ASSIGNMENT_DUE、ATTENDANCE、...）、`status`（UNREAD/READ）

### 8.2 标记已读
- `PUT /notifications/{id}/read`
- `POST /notifications/read-all`（批量标记）

### 8.3 系统公告推送（教师）
- `POST /notifications`（需教师权限）
```json
{
  "targets": ["all", "student:2021001"],
  "title": "作业提醒",
  "content": "今晚 23:59 截止",
  "type": "ASSIGNMENT_DUE"
}
```

---

## 9. 文件服务
- 上传：`POST /files`（multipart，返回 `fileId` 与 `url`）
- 下载：`GET /files/{id}`
- 删除：`DELETE /files/{id}`（仅作者可删除）
- 安全策略：仅作者或有资源权限的角色可访问

---

## 10. 统计与仪表盘
- `GET /statistics/learning`（学生视角）
  - 返回：`completedAssignments`、`averageScore`、`attendanceRate`、`discussionCount`
- `GET /statistics/course`（教师视角）
  - 返回：作业完成度曲线、出勤曲线、Top Students、成绩分布

---

## 11. 错误码
| 模块 | 错误码 | 描述 |
| --- | --- | --- |
| 认证 | 1001 | 用户不存在 |
| 认证 | 1002 | SSO ticket无效 |
| 认证 | 1003 | 未授权访问 |
| 作业 | 4001 | 作业不存在或未开放 |
| 作业 | 4002 | 超过提交次数或已截止 |
| 出勤 | 5001 | 签到已结束 |
| 出勤 | 5002 | 已签到 |
| 讨论 | 6001 | 帖子不存在 |

---

## 12. SSO对接说明
- **SSO地址**：`https://ids6.usst.edu.cn/authserver/login?service={回调URL}`
- **回调URL**：`https://{host}/api/v1/auth/sso/callback`
- **返回信息**：学号（studentNo）、姓名（name）等
- **用户同步**：首次登录自动创建用户（role=STUDENT），教师通过配置识别
- **Token生成**：SSO验证成功后生成JWT，返回前端

---

## 13. 文档与测试
- **Swagger UI**：`/swagger-ui/index.html`
- **ApiFox 项目**：导入 `API.json`（OpenAPI 3.0）
- **测试账号**：使用真实学号通过SSO登录测试

> 详细字段/枚举/示例请求响应请参考 `API.json`（OpenAPI 3.0）。
