# 项目管理与过程改进课程 API 文档（v1.0）

> 与《项目计划.md》、数据库设计、OpenAPI (`API.json`) 保持一致。遵循 RESTful 风格，统一响应格式。

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
| 认证/用户 | 登录、个人资料 | 角色管理、账号禁用 |
| 课程/班级 | 课程列表、班级成员 | 公告、资料 |
| 作业 | 作业列表/提交/批改 | 权重、统计 |
| 出勤 | 签到、记录查询 | 请假审批、报表 |
| 讨论区 | （—） | 讨论帖、评论 |
| 通知 | 作业/考勤提醒 | 自定义通知、公告推送 |
| 统计 | 基础学习统计 | 教师 Dashboard |

以下详细列出主要接口（仅展示关键字段，完整结构见 `API.json`）。

---

## 3. 认证与用户

### 3.1 登录
- `POST /auth/login`
- 请求
```json
{ "email": "student@usst.edu.cn", "password": "Passw0rd!" }
```
- 响应 `token`、`refreshToken`、用户角色。

### 3.2 刷新 Token
- `POST /auth/refresh`

### 3.3 用户详情
- `GET /users/me`
- 返回基本信息、绑定班级、角色列表。

### 3.4 更新资料
- `PUT /users/me`
- 字段：`name`、`avatarUrl`、`phone`。

### 3.5 管理员：角色/状态
- `GET /admin/users`
- `PUT /admin/users/{id}/status`

---

## 4. 课程与班级

### 4.1 课程列表
- `GET /courses`
- 查询参数：`semester`、`keyword`、分页。

### 4.2 课程详情
- `GET /courses/{courseId}`
- 包含班级、授课教师、公告摘要。

### 4.3 班级成员
- `GET /classes/{classId}/members`
- 教师可 `POST /classes/{classId}/members` 批量导入。

### 4.4 公告（迭代 2）
- `POST /classes/{classId}/announcements`
- `GET /classes/{classId}/announcements`

---

## 5. 作业与成绩

### 5.1 作业列表
- `GET /classes/{classId}/assignments`
- 过滤：`status`（ONGOING/ENDED）、`type`。

### 5.2 作业详情
- `GET /assignments/{id}`
- 返回题目描述、附件、提交规则。

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

### 5.5 教师批改
- `POST /assignments/{id}/submissions/{submissionId}/grade`
```json
{
  "score": 92,
  "feedback": "分析充分，注意格式。"
}
```

### 5.6 成绩查询
- 学生：`GET /classes/{classId}/grades/me`
- 教师：`GET /classes/{classId}/grades?assignmentId=...`

---

## 6. 出勤与请假

### 6.1 创建考勤
- `POST /classes/{classId}/attendance-sessions`
```json
{
  "title": "第 5 周",
  "mode": "CODE",
  "startAt": "...",
  "endAt": "...",
  "code": "873214"
}
```

### 6.2 签到/签退
- `POST /attendance-sessions/{sessionId}/checkin`
- `POST /attendance-sessions/{sessionId}/checkout`

### 6.3 记录查询
- 学生：`GET /attendance-sessions/my?classId=...`
- 教师：`GET /attendance-sessions/{sessionId}/records`

### 6.4 请假
- `POST /attendance-sessions/{sessionId}/leaves`
- 审批：`PUT /attendance-sessions/leaves/{leaveId}`

---

## 7. 讨论区与互动（迭代 2）

### 7.1 帖子
- `GET /classes/{classId}/discussions`
- `POST /classes/{classId}/discussions`
- 字段：`title`、`content`（支持 Markdown）、`attachmentIds`。

### 7.2 评论
- `POST /discussions/{id}/comments`
- `GET /discussions/{id}/comments`

### 7.3 点赞/置顶
- `POST /discussions/{id}/like`
- 教师置顶：`PUT /discussions/{id}/pin`

---

## 8. 通知与消息

### 8.1 获取通知
- `GET /notifications`
- 过滤：`type`（ASSIGNMENT_DUE、ATTENDANCE`, ...）、`status`（UNREAD/READ）。

### 8.2 标记已读
- `PUT /notifications/{id}/read`

### 8.3 批量标记
- `POST /notifications/read-all`

### 8.4 系统公告推送（教师/管理员）
- `POST /notifications`
```json
{
  "targets": ["class:1201", "user:87321"],
  "title": "作业提醒",
  "content": "今晚 23:59 截止",
  "type": "ASSIGNMENT_DUE"
}
```

---

## 9. 文件服务
- 上传：`POST /files`（multipart，返回 `fileId` 与 `url`）
- 下载：`GET /files/{id}`
- 删除：`DELETE /files/{id}`
- 安全策略：仅作者或有资源权限的角色可访问。

---

## 10. 统计与仪表盘
- `GET /statistics/learning?classId=...`
  - `completedAssignments`、`averageScore`、`attendanceRate`、`discussionCount`
- `GET /statistics/classes/{classId}`
  - 返回作业完成度曲线、出勤曲线、Top Students

---

## 11. 错误码
| 模块 | 错误码 | 描述 |
| --- | --- | --- |
| 认证 | 1001 | 用户不存在 |
| 认证 | 1002 | 密码错误 |
| 作业 | 4001 | 作业不存在或未开放 |
| 作业 | 4002 | 超过提交次数或已截止 |
| 出勤 | 5001 | 签到已结束 |
| 出勤 | 5002 | 已签到 |
| 讨论 | 6001 | 帖子不存在 |

---

## 12. 文档与测试
- **Swagger UI**：`/swagger-ui/index.html`
- **ApiFox 项目**：`https://apifox.cn/a/spm-course`（与 Postman/Swagger 同步，便于团队协作）
- **示例账号**：
  - 学生：`student@example.com / Passw0rd!`
  - 教师：`teacher@example.com / Passw0rd!`

> 详细字段/枚举/示例请求响应请参考 `API.json`（OpenAPI 3.0）。***

