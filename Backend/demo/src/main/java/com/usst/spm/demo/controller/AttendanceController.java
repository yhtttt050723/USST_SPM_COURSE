package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.AttendanceRecordResponse;
import com.usst.spm.demo.dto.AttendanceCheckinRequest;
import com.usst.spm.demo.dto.AttendanceCheckinResponse;
import com.usst.spm.demo.dto.AttendanceSessionCreateRequest;
import com.usst.spm.demo.dto.AttendanceSessionResponse;
import com.usst.spm.demo.dto.AttendanceStatsResponse;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.UserRepository;
import com.usst.spm.demo.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    public AttendanceController(AttendanceService attendanceService, UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
    }

    private User requireLogin(HttpServletRequest request) {
        String studentNo = (String) request.getAttribute("studentNo");
        if (studentNo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        Optional<User> userOpt = userRepository.findByStudentNo(studentNo);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在");
        }
        return userOpt.get();
    }

    private boolean isTeacherOrAdmin(User user) {
        if (user == null) return false;
        String role = user.getRole();
        return "TEACHER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * 教师发布签到
     * POST /api/attendance/sessions
     */
    @PostMapping("/sessions")
    public ResponseEntity<AttendanceSessionResponse> createSession(
            @RequestBody AttendanceSessionCreateRequest request,
            HttpServletRequest httpRequest) {
        User user = requireLogin(httpRequest);
        if (!isTeacherOrAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限");
        }
        AttendanceSessionResponse resp = attendanceService.createSession(user, request);
        return ResponseEntity.ok(resp);
    }

    /**
     * 教师结束签到
     * POST /api/attendance/sessions/{id}/end
     */
    @PostMapping("/sessions/{id}/end")
    public ResponseEntity<AttendanceSessionResponse> endSession(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        User user = requireLogin(httpRequest);
        AttendanceSessionResponse resp = attendanceService.endSession(user, id);
        return ResponseEntity.ok(resp);
    }

    /**
     * 教师分页获取签到任务列表
     * GET /api/attendance/sessions?courseId=1&page=0&size=10
     */
    @GetMapping("/sessions")
    public ResponseEntity<Page<AttendanceSessionResponse>> listSessions(
            @RequestParam(required = false, defaultValue = "1") Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        User user = requireLogin(httpRequest);
        if (!isTeacherOrAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限");
        }
        Page<AttendanceSessionResponse> resp = attendanceService.listSessions(courseId, page, size);
        return ResponseEntity.ok(resp);
    }

    /**
     * 教师查看签到记录
     * GET /api/attendance/sessions/{id}/records?page=0&size=10
     */
    @GetMapping("/sessions/{id}/records")
    public ResponseEntity<Page<AttendanceRecordResponse>> listRecords(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        User user = requireLogin(httpRequest);
        if (!isTeacherOrAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限");
        }
        Page<AttendanceRecordResponse> resp = attendanceService.listRecords(id, page, size);
        return ResponseEntity.ok(resp);
    }

    /**
     * 教师查看签到统计
     * GET /api/attendance/sessions/{id}/stats
     */
    @GetMapping("/sessions/{id}/stats")
    public ResponseEntity<AttendanceStatsResponse> stats(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        User user = requireLogin(httpRequest);
        if (!isTeacherOrAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限");
        }
        AttendanceStatsResponse resp = attendanceService.getStats(id);
        return ResponseEntity.ok(resp);
    }

    /**
     * 学生签到
     * POST /api/attendance/checkin
     */
    @PostMapping("/checkin")
    public ResponseEntity<AttendanceCheckinResponse> checkin(
            @RequestBody AttendanceCheckinRequest request,
            HttpServletRequest httpRequest) {
        User user = requireLogin(httpRequest);
        AttendanceCheckinResponse resp = attendanceService.checkin(user, request.getCode(), request.getCourseId());
        return ResponseEntity.ok(resp);
    }

    /**
     * 学生查看自己的签到记录
     * GET /api/attendance/sessions/my
     */
    @GetMapping("/sessions/my")
    public ResponseEntity<Page<AttendanceRecordResponse>> myRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        User user = requireLogin(httpRequest);
        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅学生可查看");
        }
        Page<AttendanceRecordResponse> resp = attendanceService.listMyRecords(user, page, size);
        return ResponseEntity.ok(resp);
    }
}

