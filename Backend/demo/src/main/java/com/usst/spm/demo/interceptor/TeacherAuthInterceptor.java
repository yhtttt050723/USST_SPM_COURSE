package com.usst.spm.demo.interceptor;

import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

@Component
public class TeacherAuthInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public TeacherAuthInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 前置的 AuthInterceptor 会把 studentNo 放到 request attribute 中
        String studentNo = (String) request.getAttribute("studentNo");
        if (studentNo == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return false;
        }

        Optional<User> userOpt = userRepository.findByStudentNo(studentNo);
        if (userOpt.isEmpty()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "用户不存在");
            return false;
        }

        User user = userOpt.get();

        // 如果当前请求需要教师权限，则校验角色
        if (requiresTeacher(request)) {
            if (!"TEACHER".equalsIgnoreCase(user.getRole())) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, "需要教师权限");
                return false;
            }
        }

        return true;
    }

    /**
     * 判断当前请求是否需要教师权限
     */
    private boolean requiresTeacher(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 作业：创建作业、查看提交列表、批改作业
        if ("/api/assignments".equals(uri) && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        if (uri.matches("/api/assignments/\\d+/submissions") && "GET".equalsIgnoreCase(method)) {
            return true;
        }
        if (uri.matches("/api/assignments/\\d+/submissions/\\d+/grade") && "POST".equalsIgnoreCase(method)) {
            return true;
        }

        // 公告：新增、修改、删除
        if (uri.startsWith("/api/announcements")
                && ("POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method))) {
            return true;
        }

        return false;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}

