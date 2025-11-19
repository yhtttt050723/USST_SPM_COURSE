# USST SPM 课程平台部署指南

> 本文只聚焦“如何把项目跑起来/部署上线”。项目背景、功能介绍请在其它章节补充。

---

## 1. 前置准备
- 操作系统：Windows 10/11、macOS、或任意 Linux
- 运行环境：  
  - Node.js 20+（含 npm）  
  - JDK 17+  
  - Maven Wrapper（仓库已自带 `mvnw` / `mvnw.cmd`）  
  - MySQL 8.x  
- 浏览器：Chrome / Edge / Firefox 最新版

### 拉取代码
```bash
git clone <your-repo-url> USST_SPM_COURSE
cd USST_SPM_COURSE
```

---

## 2. 数据库准备
1. **创建数据库与用户**
   ```sql
   CREATE DATABASE spm_course CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   CREATE USER 'spm_user'@'%' IDENTIFIED BY 'YourStrongPassword';
   GRANT ALL PRIVILEGES ON spm_course.* TO 'spm_user'@'%';
   FLUSH PRIVILEGES;
   ```
2. **建表**  
   - 参考 `SqldDesign.md`，将核心表以 DDL 的形式导入。  
   - 也可以把 Flyway/Liquibase 脚本放在 `Backend/demo/src/main/resources/db/migration`，由 Spring Boot 启动时自动执行（可选）。
3. **初始化数据（可选）**  
   - 插入 1 条教师账号、若干学生账号，方便后续演示。  
   - 示例：
     ```sql
     INSERT INTO users(student_no, name, password, role, status)
     VALUES ('23350001', '演示学生', '123456', 'STUDENT', 1);
     ```

---

## 3. 后端部署（Spring Boot）
> 目录：`USST_SPM_COURSE/Backend/demo`

1. **配置环境变量**
   - 复制 `src/main/resources/application.properties`，按需修改：  
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/spm_course?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
     spring.datasource.username=spm_user
     spring.datasource.password=YourStrongPassword
     ```
2. **运行（开发模式）**
   ```powershell
   cd Backend/demo
   .\mvnw clean spring-boot:run
   ```
   - 启动成功后监听 `http://localhost:8080`.
3. **打包（生产模式）**
   ```powershell
   .\mvnw clean package -DskipTests
   java -jar target/demo-*.jar
   ```
4. **常见问题排查**
   - `Public Key Retrieval is not allowed` → 在 JDBC URL 里加 `allowPublicKeyRetrieval=true`
   - `Access denied for user` → 检查 MySQL 用户/密码及授权范围
   - `Entity/JPA class not found` → 确认 `spring-boot-starter-data-jpa` 依赖已导入并执行 `mvnw clean compile`

---

## 4. 前端部署（Vue 3 + Vite）
> 目录：`USST_SPM_COURSE/frontend`

1. **安装依赖**
   ```powershell
    cd frontend
    npm install
   ```
2. **开发模式运行**
   ```powershell
   npm run dev
   ```
   - 默认访问 `http://localhost:5173`。  
   - 前端调用后端接口地址在 `src/api/auth.js` 的 `baseURL`，如需上线可改为后端部署域名。
3. **构建生产包**
   ```powershell
   npm run build
   ```
   - 生成的静态文件位于 `dist/`；可通过 Nginx/Apache/静态服务器托管。
4. **本地联调**
   - 确保后端 `http://localhost:8080` 已启动。
   - 在浏览器访问前端地址，尝试注册/登录。  
   - 使用浏览器开发者工具查看接口请求是否 200，必要时调整 CORS 设置（当前 `AuthController` 已 `@CrossOrigin("*")`）。

---

## 5. 一体化部署方案（参考）
1. **同机部署**
   - 后端 jar 使用 `systemd`/PM2/Windows 服务托管。
   - 前端打包后丢给 Nginx：  
     ```nginx
     server {
       listen 80;
       server_name spm.local;
       root /var/www/spm/dist;
       location /api/ {
         proxy_pass http://127.0.0.1:8080/api/;
       }
     }
     ```
2. **Docker Compose**
   - 编写 `docker-compose.yml`，包含 `mysql`、`backend`、`frontend`（Nginx）三个服务。  
   - 后端镜像通过 `Dockerfile` 打包：  
     ```
     FROM eclipse-temurin:17-jre
     COPY target/demo-*.jar app.jar
     ENTRYPOINT ["java","-jar","/app.jar"]
     ```
   - 前端镜像可使用 `node:20` 构建 + `nginx:alpine` 托管。

---

## 6. 验证步骤
1. `GET http://<host>:8080/actuator/health` → 返回 `{"status":"UP"}` 表示后端服务正常。  
2. `POST /api/auth/register` → 注册成功写入 `users` 表。  
3. `POST /api/auth/login` → 登录成功返回 `studentNo/name/role`。  
4. 前端页面：注册/登录表单可正常提交并收到成功提示。  
5. （可选）导入 `API.json` 到 Swagger/ApiFox，批量测试接口。

---

## 7. 目录速览
```
USST_SPM_COURSE/
├── Backend/            # Spring Boot 项目
├── frontend/           # Vue 3 + Vite 项目
├── RequirementDocument.md
├── SqldDesign.md
├── APIDpocument.md
├── API.json            # OpenAPI 规范
└── 项目计划.md
```

> 更新/部署时请保持文档同步，方便课程汇报与运维交接。其它项目介绍类内容可放在本文件最上方或独立文档里。
