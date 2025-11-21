package com.usst.spm.demo.controller;

import com.usst.spm.demo.dto.AssignmentCreateRequest;
import com.usst.spm.demo.model.Assignment;
import com.usst.spm.demo.repository.AssignmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "*")
public class AssignmentController {

    private static final Long DEFAULT_COURSE_ID = 1L;

    private final AssignmentRepository assignmentRepository;

    public AssignmentController(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @PostMapping
    public ResponseEntity<Assignment> createAssignment(@RequestBody AssignmentCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "作业标题不能为空");
        }
        if (request.getDueAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "截止时间不能为空");
        }

        Assignment assignment = new Assignment();
        assignment.setCourseId(request.getCourseId() != null ? request.getCourseId() : DEFAULT_COURSE_ID);
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setType(request.getType());
        assignment.setTotalScore(request.getTotalScore() != null ? request.getTotalScore() : 100);
        assignment.setAllowResubmit(Boolean.TRUE.equals(request.getAllowResubmit()));
        assignment.setDueAt(request.getDueAt());
        assignment.setStatus(request.getDueAt().isBefore(LocalDateTime.now()) ? "ENDED" : "ONGOING");
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());

        Assignment saved = assignmentRepository.save(assignment);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}

