package com.example.vite_coding_boot.adapter.in.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.vite_coding_boot.adapter.in.web.dto.ApproveRequest;
import com.example.vite_coding_boot.adapter.in.web.dto.AssignmentCreateRequest;
import com.example.vite_coding_boot.adapter.in.web.dto.AssignmentResponse;
import com.example.vite_coding_boot.adapter.in.web.dto.AssignmentUpdateRequest;
import com.example.vite_coding_boot.adapter.in.web.dto.FinalResultRequest;
import com.example.vite_coding_boot.adapter.in.web.dto.RejectRequest;
import com.example.vite_coding_boot.application.port.in.AssignmentUseCase;
import com.example.vite_coding_boot.domain.model.Assignment;
import com.example.vite_coding_boot.domain.model.User;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/assignments")
public class ApiAssignmentController {

    private final AssignmentUseCase assignmentUseCase;

    public ApiAssignmentController(AssignmentUseCase assignmentUseCase) {
        this.assignmentUseCase = assignmentUseCase;
    }

    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> list(HttpServletRequest request) {
        User user = getUser(request);
        List<Assignment> assignments;
        if (user.isLeader()) {
            assignments = assignmentUseCase.findAllAssignments();
        } else {
            assignments = assignmentUseCase.findAssignmentsByCreatorOrAssignee(user);
        }
        return ResponseEntity.ok(assignments.stream().map(AssignmentResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> get(@PathVariable Long id) {
        Assignment assignment = assignmentUseCase.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다: " + id));
        return ResponseEntity.ok(AssignmentResponse.from(assignment));
    }

    @PostMapping
    public ResponseEntity<AssignmentResponse> create(@RequestBody AssignmentCreateRequest req,
                                                     HttpServletRequest request) {
        User user = getUser(request);
        Assignment assignment = assignmentUseCase.createAssignment(
                req.title(), req.description(), user.getId(), req.startDate(), req.dueDate());
        return ResponseEntity.ok(AssignmentResponse.from(assignment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponse> update(@PathVariable Long id,
                                                     @RequestBody AssignmentUpdateRequest req) {
        Assignment assignment = assignmentUseCase.updateAssignment(
                id, req.title(), req.description(), req.startDate(), req.dueDate());
        return ResponseEntity.ok(AssignmentResponse.from(assignment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        User user = getUser(request);
        if (!user.isLeader()) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "FORBIDDEN", "message", "권한이 없습니다."));
        }
        assignmentUseCase.deleteAssignment(id, user.getId());
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestBody ApproveRequest req,
                                     HttpServletRequest request) {
        User user = getUser(request);
        if (!user.isLeader()) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "FORBIDDEN", "message", "권한이 없습니다."));
        }
        Assignment assignment = assignmentUseCase.approveAssignment(id, req.assigneeUserId(), user.getId());
        return ResponseEntity.ok(AssignmentResponse.from(assignment));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody RejectRequest req,
                                    HttpServletRequest request) {
        User user = getUser(request);
        if (!user.isLeader()) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "FORBIDDEN", "message", "권한이 없습니다."));
        }
        Assignment assignment = assignmentUseCase.rejectAssignment(id, req.rejectionReason(), user.getId());
        return ResponseEntity.ok(AssignmentResponse.from(assignment));
    }

    @PostMapping("/{id}/result")
    public ResponseEntity<AssignmentResponse> submitResult(@PathVariable Long id,
                                                           @RequestBody FinalResultRequest req,
                                                           HttpServletRequest request) {
        User user = getUser(request);
        Assignment assignment = assignmentUseCase.submitFinalResult(id, req.finalResult(), user.getId());
        return ResponseEntity.ok(AssignmentResponse.from(assignment));
    }

    private User getUser(HttpServletRequest request) {
        return (User) request.getAttribute("loginUser");
    }
}
