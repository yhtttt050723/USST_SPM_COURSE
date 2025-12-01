package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.LoginRequest;
import com.usst.spm.demo.dto.LoginResponse;
import com.usst.spm.demo.dto.RegisterRequest;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.usst.spm.demo.util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByStudentNo(request.getStudentNo())
                .orElseThrow(() -> new RuntimeException("学号不存在"));

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 密码验证
        // 开发阶段：统一使用 "123456" 作为默认密码（所有用户，包括教师）
        // 如果数据库中有密码，优先验证数据库中的密码（明文比较）
        String storedPassword = user.getPassword();
        boolean passwordValid = false;

        if (storedPassword != null && !storedPassword.isEmpty()) {
            // 如果是BCrypt哈希值（以 $2a$ 或 $2b$ 开头），开发阶段统一使用 "123456"
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
                // 开发阶段：所有用户（包括教师）统一使用 "123456"
                passwordValid = "123456".equals(request.getPassword());
            } else {
                // 明文密码，直接比较
                passwordValid = storedPassword.equals(request.getPassword());
            }
        } else {
            // 如果没有密码，默认使用 "123456"
            passwordValid = "123456".equals(request.getPassword());
        }

        if (!passwordValid) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getStudentNo(), user.getRole());

        LoginResponse resp = new LoginResponse(user.getId(), user.getStudentNo(), user.getName(), user.getRole(),token);



        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest req) {
        if (userRepository.existsByStudentNo(req.getStudentNo())) {
            throw new RuntimeException("学号已存在");
        }
        User user = new User();
        user.setStudentNo(req.getStudentNo());
        user.setName(req.getName());
        user.setPassword(req.getPassword()); // 先明文，后续可加密
        user.setRole("STUDENT");
        user.setStatus(1);
        user.setDeleted(0);
        user = userRepository.save(user);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getStudentNo(), user.getRole());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse(user.getId(), user.getStudentNo(), user.getName(), user.getRole(),token));
    }
}