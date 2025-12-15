package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.GradeHistoryResponse;
import com.usst.spm.demo.dto.UpdateScoreRequest;
import com.usst.spm.demo.model.Grade;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.UserRepository;
import com.usst.spm.demo.service.AssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * 成绩管理Controller
 * 处理成绩修改和成绩历史查询
 */
@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
public class ScoreController {

    private final AssignmentService assignmentService;
    private final UserRepository userRepository;

    public ScoreController(AssignmentService assignmentService, UserRepository userRepository) {
        this.assignmentService = assignmentService;
        this.userRepository = userRepository;
    }

    /**
     * 获取当前用户ID和角色
     */
    private UserInfo getCurrentUserInfo(HttpServletRequest request) {
        String studentNo = (String) request.getAttribute("studentNo");
        if (studentNo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        Optional<User> userOpt = userRepository.findByStudentNo(studentNo);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在");
        }
        User user = userOpt.get();
        return new UserInfo(user.getId(), user.getRole());
    }

    /**
     * 修改学生成绩
     * PUT /api/submissions/{id}/score
     * 
     * 权限：教师
     * 
     * 入参：
     * {
     *   "newScore": 85,
     *   "reason": "修改原因（必填）",
     *   "feedback": "评语（可选）"
     * }
     * 
     * 返回：Grade对象
     */
    @PutMapping("/{id}/score")
    public ResponseEntity<Grade> updateScore(
            @PathVariable Long id,
            @RequestBody UpdateScoreRequest request,
            HttpServletRequest httpRequest) {
        // 权限校验：只有教师可以修改成绩
        UserInfo userInfo = getCurrentUserInfo(httpRequest);
        if (!"TEACHER".equals(userInfo.role) && !"ADMIN".equals(userInfo.role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有教师可以修改成绩");
        }

        Grade grade = assignmentService.updateScore(id, request, userInfo.userId, userInfo.role);
        return ResponseEntity.ok(grade);
    }

    /**
     * 查询成绩历史
     * GET /api/submissions/{id}/score-history
     * 
     * 权限：
     * - 教师：可以查看自己作业的所有提交的成绩历史
     * - 管理员：可以查看所有成绩历史
     * - 学生：只能查看自己的成绩历史
     * 
     * 返回：成绩历史列表
     */
    @GetMapping("/{id}/score-history")
    public ResponseEntity<List<GradeHistoryResponse>> getScoreHistory(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        UserInfo userInfo = getCurrentUserInfo(httpRequest);

        List<GradeHistoryResponse> histories = assignmentService.getScoreHistory(
                id, userInfo.userId, userInfo.role);
        return ResponseEntity.ok(histories);
    }

    /**
     * 用户信息内部类
     */
    private static class UserInfo {
        Long userId;
        String role;

        UserInfo(Long userId, String role) {
            this.userId = userId;
            this.role = role;
        }
    }
}

