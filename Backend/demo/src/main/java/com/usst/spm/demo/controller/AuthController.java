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
        if (!"123456".equals(request.getPassword())) {
            throw new RuntimeException("密码错误（暂时固定123456）");
        }

        String token = jwtUtil.generateToken(user.getStudentNo(), user.getRole());

        LoginResponse resp = new LoginResponse(user.getStudentNo(), user.getName(), user.getRole(), token);
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
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getStudentNo(), user.getRole());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse(user.getStudentNo(), user.getName(), user.getRole(), token));
    }
}