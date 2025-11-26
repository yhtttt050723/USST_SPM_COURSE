package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.UserProfileResponse;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    // 依赖注入
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * GET /api/users/me - 获取当前登录用户信息
     * 对应 API 文档中的 "获取当前用户" 接口
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(HttpServletRequest request) {

        // 获取学号
        String studentNo = (String) request.getAttribute("studentNo");

        if (studentNo == null) {
            throw new RuntimeException("Authentication context not found. Please ensure token is valid.");
        }

        // 根据学号查询完整的用户信息
        User user = userRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new RuntimeException("User data not found in database.")); // 用户数据丢失

        // 封装并返回响应 DTO (只包含非敏感信息)
        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getStudentNo(),
                user.getName(),
                user.getRole(),
                user.getStatus()
        );

        return ResponseEntity.ok(response);
    }
}