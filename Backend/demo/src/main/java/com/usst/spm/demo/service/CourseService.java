package com.usst.spm.demo.service;

import com.usst.spm.demo.dto.CourseResponse;
import com.usst.spm.demo.dto.InviteCreateRequest;
import com.usst.spm.demo.model.Course;
import com.usst.spm.demo.model.CourseEnrollment;
import com.usst.spm.demo.model.CourseInviteCode;
import com.usst.spm.demo.model.User;
import com.usst.spm.demo.repository.CourseEnrollmentRepository;
import com.usst.spm.demo.repository.CourseInviteCodeRepository;
import com.usst.spm.demo.repository.CourseRepository;
import com.usst.spm.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final CourseInviteCodeRepository courseInviteCodeRepository;
    private final UserRepository userRepository;

    public CourseService(
            CourseRepository courseRepository,
            CourseEnrollmentRepository courseEnrollmentRepository,
            CourseInviteCodeRepository courseInviteCodeRepository,
            UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.courseEnrollmentRepository = courseEnrollmentRepository;
        this.courseInviteCodeRepository = courseInviteCodeRepository;
        this.userRepository = userRepository;
    }

    private User requireUser(String studentNo) {
        return userRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
    }

    private boolean isTeacherOrAdmin(User user) {
        if (user == null) return false;
        String role = user.getRole();
        return "TEACHER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    private CourseResponse toResponse(Course course, String roleInCourse) {
        CourseResponse resp = new CourseResponse();
        resp.setId(course.getId());
        resp.setName(course.getName());
        resp.setCode(course.getCode());
        resp.setAcademicYear(course.getAcademicYear());
        resp.setTerm(course.getTerm());
        resp.setSemester(course.getSemester());
        resp.setDescription(course.getDescription());
        resp.setTeacherId(course.getTeacherId());
        resp.setInviteCode(course.getInviteCode());
        resp.setInviteExpireAt(course.getInviteExpireAt());
        resp.setRoleInCourse(roleInCourse);
        return resp;
    }

    public List<CourseResponse> listMyCourses(String studentNo) {
        User user = requireUser(studentNo);
        List<CourseResponse> responses = new ArrayList<>();

        // 教师：我管理的课程
        if (isTeacherOrAdmin(user)) {
            List<Course> teaching = courseRepository.findByTeacherIdAndDeleted(user.getId(), 0);
            responses.addAll(teaching.stream()
                    .map(c -> toResponse(c, "TEACHER"))
                    .collect(Collectors.toList()));
        }

        // 学生/TA：我加入的课程
        List<CourseEnrollment> enrollments = courseEnrollmentRepository
                .findByStudentIdAndStatusAndDeleted(user.getId(), "ACTIVE", 0);
        if (!enrollments.isEmpty()) {
            List<Long> courseIds = enrollments.stream()
                    .map(CourseEnrollment::getCourseId)
                    .distinct()
                    .toList();
            List<Course> courses = courseIds.isEmpty() ? List.of() : courseRepository.findByIdIn(courseIds);
            Map<Long, Course> courseMap = courses.stream()
                    .collect(Collectors.toMap(Course::getId, c -> c));
            for (CourseEnrollment enrollment : enrollments) {
                Course course = courseMap.get(enrollment.getCourseId());
                if (course != null && (course.getDeleted() == null || course.getDeleted() == 0)) {
                    responses.add(toResponse(course, enrollment.getRole()));
                }
            }
        }

        // 去重（如果教师同时被加入为学生，保留教师视角）
        Map<Long, CourseResponse> unique = new LinkedHashMap<>();
        for (CourseResponse resp : responses) {
            unique.putIfAbsent(resp.getId(), resp);
        }
        return new ArrayList<>(unique.values());
    }

    public CourseResponse getCourse(String studentNo, Long courseId) {
        User user = requireUser(studentNo);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "课程不存在"));
        if (course.getDeleted() != null && course.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "课程不存在");
        }

        String roleInCourse = "UNKNOWN";
        if (Objects.equals(course.getTeacherId(), user.getId())) {
            roleInCourse = "TEACHER";
        } else {
            Optional<CourseEnrollment> enrollment = courseEnrollmentRepository
                    .findByCourseIdAndStudentIdAndDeleted(courseId, user.getId(), 0);
            if (enrollment.isPresent()) {
                roleInCourse = enrollment.get().getRole();
            }
        }
        return toResponse(course, roleInCourse);
    }

    @Transactional
    public Map<String, Object> generateInvite(String studentNo, Long courseId, InviteCreateRequest request) {
        User user = requireUser(studentNo);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "课程不存在"));
        if (!Objects.equals(course.getTeacherId(), user.getId()) && !isTeacherOrAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限生成邀请码");
        }

        int expireDays = request != null && request.getExpireDays() != null ? request.getExpireDays() : 30;
        int maxUse = request != null && request.getMaxUse() != null ? request.getMaxUse() : 0;
        LocalDateTime expireAt = LocalDateTime.now().plusDays(expireDays);
        String code = randomCode(6);

        log.info("[invite] generate request studentNo={} courseId={} expireDays={} maxUse={}", studentNo, courseId, expireDays, maxUse);

        CourseInviteCode invite = new CourseInviteCode();
        invite.setCourseId(courseId);
        invite.setCode(code);
        invite.setExpireAt(expireAt);
        invite.setMaxUse(maxUse);
        invite.setActive(1);
        invite.setUsedCount(0);
        courseInviteCodeRepository.save(invite);

        // 更新课程当前有效码
        course.setInviteCode(code);
        course.setInviteExpireAt(expireAt);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);

        Map<String, Object> resp = new HashMap<>();
        resp.put("inviteCode", code);
        resp.put("expireAt", expireAt);
        resp.put("maxUse", maxUse);
        log.info("[invite] generated inviteCode={} expireAt={} maxUse={} courseId={}", code, expireAt, maxUse, courseId);
        return resp;
    }

    @Transactional
    public void revokeInvite(String studentNo, Long courseId, String code) {
        User user = requireUser(studentNo);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "课程不存在"));
        if (!Objects.equals(course.getTeacherId(), user.getId()) && !isTeacherOrAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限操作");
        }

        courseInviteCodeRepository.findByCodeAndActive(code, 1).ifPresent(invite -> {
            invite.setActive(0);
            invite.setUpdatedAt(LocalDateTime.now());
            courseInviteCodeRepository.save(invite);
        });

        if (code.equals(course.getInviteCode())) {
            course.setInviteCode(null);
            course.setInviteExpireAt(null);
            course.setUpdatedAt(LocalDateTime.now());
            courseRepository.save(course);
        }
    }

    @Transactional
    public CourseResponse joinByCode(String studentNo, String code) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邀请码不能为空");
        }
        log.info("[invite] join attempt studentNo={} code={}", studentNo, code);
        User user = requireUser(studentNo);

        // 先查激活的邀请码表
        Optional<CourseInviteCode> inviteOpt = courseInviteCodeRepository.findByCodeAndActive(code, 1);
        Course course = null;
        CourseInviteCode invite = null;

        if (inviteOpt.isPresent()) {
            invite = inviteOpt.get();
            course = courseRepository.findById(invite.getCourseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "课程不存在"));
            validateInvite(invite);
        } else {
            // 兜底：使用 course 表当前邀请码
            course = courseRepository.findByInviteCodeAndDeleted(code, 0)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "邀请码无效"));
            if (course.getInviteExpireAt() != null && LocalDateTime.now().isAfter(course.getInviteExpireAt())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邀请码已过期");
            }
        }

        // 已加入则直接返回
        Optional<CourseEnrollment> exist = courseEnrollmentRepository
                .findByCourseIdAndStudentIdAndDeleted(course.getId(), user.getId(), 0);
        if (exist.isPresent()) {
            return toResponse(course, exist.get().getRole());
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourseId(course.getId());
        enrollment.setStudentId(user.getId());
        enrollment.setRole("STUDENT");
        enrollment.setStatus("ACTIVE");
        courseEnrollmentRepository.save(enrollment);

        if (invite != null) {
            Integer used = invite.getUsedCount() == null ? 0 : invite.getUsedCount();
            invite.setUsedCount(used + 1);
            invite.setUpdatedAt(LocalDateTime.now());
            courseInviteCodeRepository.save(invite);
            log.info("[invite] join success studentNo={} courseId={} code={} usedCount={}", studentNo, course.getId(), code, invite.getUsedCount());
        } else {
            log.info("[invite] join success studentNo={} courseId={} code={} (from course current code)", studentNo, course.getId(), code);
        }

        return toResponse(course, "STUDENT");
    }

    private void validateInvite(CourseInviteCode invite) {
        if (invite.getActive() != null && invite.getActive() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邀请码已失效");
        }
        if (invite.getExpireAt() != null && LocalDateTime.now().isAfter(invite.getExpireAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邀请码已过期");
        }
        if (invite.getMaxUse() != null && invite.getMaxUse() > 0) {
            int used = invite.getUsedCount() == null ? 0 : invite.getUsedCount();
            if (used >= invite.getMaxUse()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "邀请码已达上限");
            }
        }
    }

    private String randomCode(int length) {
        String chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

