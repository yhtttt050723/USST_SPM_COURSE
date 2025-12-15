package com.usst.spm.demo.service;

import com.usst.spm.demo.dto.AttendanceRecordResponse;
import com.usst.spm.demo.dto.AttendanceSessionCreateRequest;
import com.usst.spm.demo.dto.AttendanceSessionResponse;
import com.usst.spm.demo.dto.AttendanceStatsResponse;
import com.usst.spm.demo.dto.AttendanceCheckinResponse;
import com.usst.spm.demo.dto.AttendanceCheckinRequest;
import com.usst.spm.demo.model.AttendanceRecord;
import com.usst.spm.demo.model.AttendanceSession;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.AttendanceRecordRepository;
import com.usst.spm.demo.repository.AttendanceSessionRepository;
import com.usst.spm.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public AttendanceService(AttendanceSessionRepository sessionRepository,
                             AttendanceRecordRepository recordRepository,
                             UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    private boolean isTeacherOrAdmin(User user) {
        if (user == null) return false;
        String role = user.getRole();
        return "TEACHER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    private String generateCode(Long courseId) {
        for (int i = 0; i < 20; i++) {
            String code = String.format("%04d", random.nextInt(10000));
            boolean exists = sessionRepository
                    .findFirstByCourseIdAndCodeAndStatusAndEndTimeAfter(courseId, code, "ACTIVE", LocalDateTime.now())
                    .isPresent();
            if (!exists) {
                return code;
            }
        }
        return String.format("%04d", random.nextInt(10000));
    }

    public AttendanceSessionResponse createSession(User currentUser, AttendanceSessionCreateRequest request) {
        if (!isTeacherOrAdmin(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限");
        }
        Long courseId = request.getCourseId() == null ? 1L : request.getCourseId();
        request.setCourseId(courseId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = request.getEndTime();
        if (end == null) {
            int minutes = request.getDurationMinutes() != null && request.getDurationMinutes() > 0
                    ? request.getDurationMinutes()
                    : 10;
            end = now.plusMinutes(minutes);
        }
        if (end.isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "结束时间必须晚于当前时间");
        }

        String code = request.getCode();
        if (code == null || code.isBlank()) {
            code = generateCode(request.getCourseId());
        } else {
            if (!code.matches("^\\d{4}$")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "签到码需为4位数字");
            }
            boolean exists = sessionRepository
                    .findFirstByCourseIdAndCodeAndStatusAndEndTimeAfter(courseId, code, "ACTIVE", now)
                    .isPresent();
            if (exists) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "该课程存在相同签到码的进行中签到");
            }
        }

        AttendanceSession session = new AttendanceSession();
        session.setCourseId(courseId);
        session.setTeacherId(currentUser.getId());
        session.setTitle(request.getTitle());
        session.setCode(code);
        session.setStatus("ACTIVE");
        session.setStartTime(now);
        session.setEndTime(end);

        AttendanceSession saved = sessionRepository.save(session);
        return toSessionResponse(saved);
    }

    public AttendanceSessionResponse endSession(User currentUser, Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "签到不存在"));
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        boolean sameTeacher = session.getTeacherId().equals(currentUser.getId());
        if (!isTeacherOrAdmin(currentUser) || (!isAdmin && !sameTeacher)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限结束此签到");
        }
        if ("ENDED".equalsIgnoreCase(session.getStatus())) {
            return toSessionResponse(session);
        }
        session.setStatus("ENDED");
        AttendanceSession saved = sessionRepository.save(session);
        return toSessionResponse(saved);
    }

    public Page<AttendanceSessionResponse> listSessions(Long courseId, int page, int size) {
        Long finalCourseId = courseId == null ? 1L : courseId;
        Page<AttendanceSession> data = sessionRepository.findByCourseIdOrderByStartTimeDesc(
                finalCourseId, PageRequest.of(page, size));
        return data.map(this::toSessionResponse);
    }

    public Page<AttendanceRecordResponse> listRecords(Long sessionId, int page, int size) {
        Page<AttendanceRecord> data = recordRepository.findBySessionIdOrderByCheckinAtDesc(
                sessionId, PageRequest.of(page, size));
        return data.map(this::toRecordResponse);
    }

    public AttendanceStatsResponse getStats(Long sessionId) {
        long present = recordRepository.countBySessionId(sessionId);
        AttendanceStatsResponse resp = new AttendanceStatsResponse();
        resp.setSessionId(sessionId);
        resp.setPresentCount(present);
        return resp;
    }

    public Page<AttendanceRecordResponse> listMyRecords(User student, int page, int size) {
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        Page<AttendanceRecord> data = recordRepository.findByStudentIdOrderByCheckinAtDesc(
                student.getId(), PageRequest.of(page, size));
        return data.map(this::toRecordResponse);
    }

    private AttendanceSessionResponse toSessionResponse(AttendanceSession session) {
        AttendanceSessionResponse resp = new AttendanceSessionResponse();
        resp.setId(session.getId());
        resp.setCourseId(session.getCourseId());
        resp.setTeacherId(session.getTeacherId());
        resp.setTitle(session.getTitle());
        resp.setCode(session.getCode());
        resp.setStatus(session.getStatus());
        resp.setStartTime(session.getStartTime());
        resp.setEndTime(session.getEndTime());
        return resp;
    }

    private AttendanceRecordResponse toRecordResponse(AttendanceRecord record) {
        AttendanceRecordResponse resp = new AttendanceRecordResponse();
        resp.setId(record.getId());
        resp.setSessionId(record.getSessionId());
        resp.setStudentId(record.getStudentId());
        resp.setStatus(record.getStatus());
        resp.setCheckinTime(record.getCheckinAt());
        resp.setResult(record.getResult());
        resp.setRemark(record.getRemark());

        userRepository.findById(record.getStudentId()).ifPresent(user -> {
            resp.setStudentName(user.getName());
            resp.setStudentNo(user.getStudentNo());
        });
        return resp;
    }

    /**
     * 学生签到
     */
    public AttendanceCheckinResponse checkin(User student, String code, Long courseId) {
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        if (!"STUDENT".equalsIgnoreCase(student.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅学生可签到");
        }
        if (code == null || !code.matches("^\\d{4}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "签到码需为4位数字");
        }
        Long finalCourseId = courseId == null ? 1L : courseId;

        LocalDateTime now = LocalDateTime.now();
        AttendanceSession session = sessionRepository
                .findFirstByCourseIdAndCodeAndStatusAndEndTimeAfter(finalCourseId, code, "ACTIVE", now)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "签到码无效"));

        // 过期检测
        if (session.getEndTime() != null && now.isAfter(session.getEndTime())) {
            session.setStatus("EXPIRED");
            sessionRepository.save(session);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "签到已过期");
        }
        if (!"ACTIVE".equalsIgnoreCase(session.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "签到已结束");
        }

        // 防重复
        boolean exists = recordRepository.findBySessionIdAndStudentId(session.getId(), student.getId()).isPresent();
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "重复签到");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setSessionId(session.getId());
        record.setStudentId(student.getId());
        record.setStatus("PRESENT");
        record.setResult("SUCCESS");
        record.setCheckinAt(now);
        try {
            recordRepository.save(record);
        } catch (DataIntegrityViolationException e) {
            // 唯一索引兜底防并发重复
            throw new ResponseStatusException(HttpStatus.CONFLICT, "重复签到");
        }

        AttendanceCheckinResponse resp = new AttendanceCheckinResponse();
        resp.setSessionId(session.getId());
        resp.setCheckinTime(now);
        resp.setMessage("签到成功");
        return resp;
    }
}

