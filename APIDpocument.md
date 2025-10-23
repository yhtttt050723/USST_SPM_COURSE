# 项目管理与过程改进课程网站 API 接口文档

## 1. 接口概述

### 1.1 基础信息
- **基础URL**: `http://localhost:8080/api`
- **数据格式**: JSON
- **字符编码**: UTF-8
- **认证方式**: JWT Token

### 1.2 通用响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 1.3 状态码说明
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 禁止访问
- `404`: 资源不存在
- `500`: 服务器内部错误

## 2. 用户管理模块

### 2.1 用户注册
**接口地址**: `POST /auth/register`

**请求参数**:
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "role": "string",
  "studentId": "string"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "张三",
    "email": "zhangsan@example.com",
    "role": "STUDENT"
  }
}
```

### 2.2 用户登录
**接口地址**: `POST /auth/login`

**请求参数**:
```json
{
  "email": "string",
  "password": "string"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "userId": 1,
      "username": "张三",
      "email": "zhangsan@example.com",
      "role": "STUDENT"
    }
  }
}
```

### 2.3 获取用户信息
**接口地址**: `GET /user/profile`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "张三",
    "email": "zhangsan@example.com",
    "role": "STUDENT",
    "avatar": "http://localhost:8080/avatar/1.jpg",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

### 2.4 更新用户信息
**接口地址**: `PUT /user/profile`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "username": "string",
  "avatar": "string"
}
```

## 3. 课程管理模块

### 3.1 获取课程列表
**接口地址**: `GET /courses`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `page`: 页码 (默认: 1)
- `size`: 每页数量 (默认: 10)
- `keyword`: 搜索关键词

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "courses": [
      {
        "courseId": 1,
        "courseName": "项目管理基础",
        "description": "项目管理基础课程",
        "teacherName": "李老师",
        "semester": "2024春季",
        "status": "ACTIVE"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 10
  }
}
```

### 3.2 获取课程详情
**接口地址**: `GET /courses/{courseId}`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "courseId": 1,
    "courseName": "项目管理基础",
    "description": "项目管理基础课程",
    "teacherName": "李老师",
    "semester": "2024春季",
    "status": "ACTIVE",
    "announcements": [
      {
        "announcementId": 1,
        "title": "课程通知",
        "content": "请按时完成作业",
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ]
  }
}
```

### 3.3 创建课程
**接口地址**: `POST /courses`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "courseName": "string",
  "description": "string",
  "semester": "string"
}
```

## 4. 题目管理模块

### 4.1 获取题目列表
**接口地址**: `GET /questions`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `page`: 页码
- `size`: 每页数量
- `category`: 题目分类
- `difficulty`: 难度等级
- `keyword`: 搜索关键词

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "questions": [
      {
        "questionId": 1,
        "title": "项目管理的五大过程组是什么？",
        "type": "MULTIPLE_CHOICE",
        "difficulty": "MEDIUM",
        "category": "项目管理基础",
        "options": ["A", "B", "C", "D"],
        "correctAnswer": "A"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

### 4.2 获取题目详情
**接口地址**: `GET /questions/{questionId}`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "questionId": 1,
    "title": "项目管理的五大过程组是什么？",
    "content": "请选择项目管理的五大过程组：",
    "type": "MULTIPLE_CHOICE",
    "difficulty": "MEDIUM",
    "category": "项目管理基础",
    "options": [
      "启动、规划、执行、监控、收尾",
      "计划、执行、检查、行动",
      "需求、设计、开发、测试、部署",
      "分析、设计、实现、测试、维护"
    ],
    "correctAnswer": "A",
    "explanation": "项目管理的五大过程组是启动、规划、执行、监控、收尾"
  }
}
```

### 4.3 创建题目
**接口地址**: `POST /questions`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "title": "string",
  "content": "string",
  "type": "MULTIPLE_CHOICE",
  "difficulty": "EASY",
  "category": "string",
  "options": ["string"],
  "correctAnswer": "string",
  "explanation": "string"
}
```

## 5. 刷题系统模块

### 5.1 开始练习
**接口地址**: `POST /practice/start`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "category": "string",
  "difficulty": "EASY",
  "questionCount": 10
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "practiceId": 1,
    "questions": [
      {
        "questionId": 1,
        "title": "项目管理的五大过程组是什么？",
        "type": "MULTIPLE_CHOICE",
        "options": ["A", "B", "C", "D"]
      }
    ],
    "totalQuestions": 10,
    "timeLimit": 1800
  }
}
```

### 5.2 提交答案
**接口地址**: `POST /practice/{practiceId}/submit`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "answers": [
    {
      "questionId": 1,
      "answer": "A"
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "practiceId": 1,
    "score": 85,
    "correctCount": 8,
    "totalCount": 10,
    "timeSpent": 1200,
    "results": [
      {
        "questionId": 1,
        "userAnswer": "A",
        "correctAnswer": "A",
        "isCorrect": true
      }
    ]
  }
}
```

### 5.3 获取练习历史
**接口地址**: `GET /practice/history`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `page`: 页码
- `size`: 每页数量

## 6. 作业管理模块

### 6.1 获取作业列表
**接口地址**: `GET /assignments`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `courseId`: 课程ID
- `status`: 作业状态

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "assignments": [
      {
        "assignmentId": 1,
        "title": "项目管理案例分析",
        "description": "分析一个实际项目案例",
        "courseId": 1,
        "dueDate": "2024-01-15T23:59:59Z",
        "status": "ACTIVE",
        "submitted": false
      }
    ]
  }
}
```

### 6.2 获取作业详情
**接口地址**: `GET /assignments/{assignmentId}`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "assignmentId": 1,
    "title": "项目管理案例分析",
    "description": "分析一个实际项目案例",
    "courseId": 1,
    "dueDate": "2024-01-15T23:59:59Z",
    "status": "ACTIVE",
    "questions": [
      {
        "questionId": 1,
        "title": "项目背景分析",
        "type": "ESSAY",
        "maxScore": 50
      }
    ]
  }
}
```

### 6.3 提交作业
**接口地址**: `POST /assignments/{assignmentId}/submit`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "answers": [
    {
      "questionId": 1,
      "answer": "string",
      "attachments": ["file1.pdf", "file2.docx"]
    }
  ]
}
```

### 6.4 批改作业
**接口地址**: `POST /assignments/{assignmentId}/grade`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "submissionId": 1,
  "scores": [
    {
      "questionId": 1,
      "score": 45,
      "feedback": "分析深入，但缺少具体案例"
    }
  ],
  "totalScore": 45,
  "feedback": "整体表现良好"
}
```

## 7. 出勤管理模块

### 7.1 签到
**接口地址**: `POST /attendance/checkin`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "courseId": 1,
  "location": "string"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "签到成功",
  "data": {
    "attendanceId": 1,
    "checkinTime": "2024-01-01T08:00:00Z",
    "status": "PRESENT"
  }
}
```

### 7.2 签退
**接口地址**: `POST /attendance/checkout`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "attendanceId": 1
}
```

### 7.3 获取出勤记录
**接口地址**: `GET /attendance/records`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `courseId`: 课程ID
- `startDate`: 开始日期
- `endDate`: 结束日期

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "attendanceId": 1,
        "courseId": 1,
        "checkinTime": "2024-01-01T08:00:00Z",
        "checkoutTime": "2024-01-01T10:00:00Z",
        "status": "PRESENT",
        "duration": 7200
      }
    ],
    "attendanceRate": 85.5
  }
}
```

### 7.4 请假申请
**接口地址**: `POST /attendance/leave`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "courseId": 1,
  "leaveDate": "2024-01-01",
  "reason": "string",
  "attachments": ["leave_request.pdf"]
}
```

## 8. 成绩管理模块

### 8.1 获取成绩列表
**接口地址**: `GET /grades`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `courseId`: 课程ID
- `type`: 成绩类型

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "grades": [
      {
        "gradeId": 1,
        "courseId": 1,
        "assignmentId": 1,
        "score": 85,
        "maxScore": 100,
        "percentage": 85.0,
        "grade": "B+",
        "submittedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "averageScore": 82.5,
    "totalAssignments": 5,
    "completedAssignments": 4
  }
}
```

### 8.2 获取成绩详情
**接口地址**: `GET /grades/{gradeId}`

**请求头**: `Authorization: Bearer {token}`

### 8.3 录入成绩
**接口地址**: `POST /grades`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "studentId": 1,
  "courseId": 1,
  "assignmentId": 1,
  "score": 85,
  "feedback": "表现良好"
}
```

## 9. 消息通知模块

### 9.1 获取消息列表
**接口地址**: `GET /messages`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `type`: 消息类型
- `status`: 消息状态

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "messages": [
      {
        "messageId": 1,
        "title": "作业提醒",
        "content": "项目管理作业即将截止",
        "type": "ASSIGNMENT",
        "status": "UNREAD",
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "unreadCount": 3
  }
}
```

### 9.2 发送消息
**接口地址**: `POST /messages`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "recipientId": 1,
  "title": "string",
  "content": "string",
  "type": "NOTIFICATION"
}
```

### 9.3 标记消息已读
**接口地址**: `PUT /messages/{messageId}/read`

**请求头**: `Authorization: Bearer {token}`

## 10. 文件管理模块

### 10.1 上传文件
**接口地址**: `POST /files/upload`

**请求头**: `Authorization: Bearer {token}`

**请求类型**: `multipart/form-data`

**请求参数**:
- `file`: 文件
- `type`: 文件类型

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "fileId": 1,
    "fileName": "document.pdf",
    "fileSize": 1024000,
    "fileUrl": "http://localhost:8080/files/1.pdf",
    "uploadedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 10.2 下载文件
**接口地址**: `GET /files/{fileId}/download`

**请求头**: `Authorization: Bearer {token}`

### 10.3 删除文件
**接口地址**: `DELETE /files/{fileId}`

**请求头**: `Authorization: Bearer {token}`

## 11. 统计分析模块

### 11.1 获取学习统计
**接口地址**: `GET /statistics/learning`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `courseId`: 课程ID
- `startDate`: 开始日期
- `endDate`: 结束日期

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalQuestions": 100,
    "answeredQuestions": 85,
    "correctAnswers": 72,
    "accuracy": 84.7,
    "studyTime": 3600,
    "practiceCount": 15,
    "averageScore": 82.5
  }
}
```

### 11.2 获取课程统计
**接口地址**: `GET /statistics/course/{courseId}`

**请求头**: `Authorization: Bearer {token}`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalStudents": 50,
    "activeStudents": 45,
    "averageAttendance": 88.5,
    "averageScore": 82.3,
    "assignmentCompletion": 90.0,
    "topPerformers": [
      {
        "studentId": 1,
        "studentName": "张三",
        "averageScore": 95.0
      }
    ]
  }
}
```

## 12. 系统管理模块

### 12.1 获取系统信息
**接口地址**: `GET /admin/system/info`

**请求头**: `Authorization: Bearer {token}`

### 12.2 获取用户列表
**接口地址**: `GET /admin/users`

**请求头**: `Authorization: Bearer {token}`

**查询参数**:
- `page`: 页码
- `size`: 每页数量
- `role`: 用户角色
- `status`: 用户状态

### 12.3 禁用/启用用户
**接口地址**: `PUT /admin/users/{userId}/status`

**请求头**: `Authorization: Bearer {token}`

**请求参数**:
```json
{
  "status": "DISABLED"
}
```

## 13. 错误处理

### 13.1 错误响应格式
```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "timestamp": "2024-01-01T00:00:00Z",
  "errors": [
    {
      "field": "email",
      "message": "邮箱格式不正确"
    }
  ]
}
```

### 13.2 常见错误码
- `1001`: 用户不存在
- `1002`: 密码错误
- `1003`: 用户已被禁用
- `1004`: Token已过期
- `2001`: 课程不存在
- `2002`: 无权限访问课程
- `3001`: 题目不存在
- `4001`: 作业不存在
- `4002`: 作业已截止
- `5001`: 出勤记录不存在
- `6001`: 成绩不存在

## 14. 接口测试

### 14.1 测试环境
- **测试URL**: `http://localhost:8080/api`
- **测试账号**: 
  - 教师: `teacher@example.com` / `password123`
  - 学生: `student@example.com` / `password123`

### 14.2 测试用例
1. 用户注册登录测试
2. 课程管理功能测试
3. 题目练习功能测试
4. 作业提交批改测试
5. 出勤管理功能测试
6. 成绩查询统计测试
7. 消息通知功能测试
8. 文件上传下载测试

## 15. 版本更新

### 15.1 版本历史
- **v1.0.0**: 初始版本，基础功能实现
- **v1.1.0**: 增加统计分析功能
- **v1.2.0**: 优化用户界面和体验
- **v1.3.0**: 增加移动端支持

### 15.2 更新计划
- 增加在线考试功能
- 增加视频教学功能
- 增加讨论区功能
- 增加学习路径推荐
