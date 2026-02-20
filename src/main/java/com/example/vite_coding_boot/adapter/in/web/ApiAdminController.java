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

import com.example.vite_coding_boot.adapter.in.web.dto.AuditLogResponse;
import com.example.vite_coding_boot.adapter.in.web.dto.ResetPasswordRequest;
import com.example.vite_coding_boot.adapter.in.web.dto.TeamCreateRequest;
import com.example.vite_coding_boot.adapter.in.web.dto.TeamResponse;
import com.example.vite_coding_boot.adapter.in.web.dto.TeamUpdateRequest;
import com.example.vite_coding_boot.adapter.in.web.dto.UserCreateRequest;
import com.example.vite_coding_boot.adapter.in.web.dto.UserResponse;
import com.example.vite_coding_boot.adapter.in.web.dto.UserUpdateRequest;
import com.example.vite_coding_boot.application.port.in.AdminUseCase;
import com.example.vite_coding_boot.application.port.in.UserQueryUseCase;
import com.example.vite_coding_boot.domain.model.Role;
import com.example.vite_coding_boot.domain.model.User;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
public class ApiAdminController {

    private final AdminUseCase adminUseCase;
    private final UserQueryUseCase userQueryUseCase;

    public ApiAdminController(AdminUseCase adminUseCase, UserQueryUseCase userQueryUseCase) {
        this.adminUseCase = adminUseCase;
        this.userQueryUseCase = userQueryUseCase;
    }

    // ── Users ──

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(HttpServletRequest request) {
        User performer = requireLeader(request);
        List<UserResponse> users = adminUseCase.findAllUsers().stream().map(UserResponse::from).toList();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody UserCreateRequest req, HttpServletRequest request) {
        User performer = requireLeader(request);
        User created = userQueryUseCase.createUser(req.username(), req.password(), req.name(), Role.valueOf(req.role()));
        if (req.teamId() != null) {
            adminUseCase.updateUser(created.getId(), req.name(), req.role(), req.teamId(), performer.getId());
        }
        User fresh = adminUseCase.findUserById(created.getId()).orElse(created);
        return ResponseEntity.ok(UserResponse.from(fresh));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest req,
                                        HttpServletRequest request) {
        User performer = requireLeader(request);
        adminUseCase.updateUser(id, req.name(), req.role(), req.teamId(), performer.getId());
        User updated = adminUseCase.findUserById(id).orElseThrow();
        return ResponseEntity.ok(UserResponse.from(updated));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        User performer = requireLeader(request);
        adminUseCase.deleteUser(id, performer.getId());
        return ResponseEntity.ok(Map.of("message", "사용자가 삭제되었습니다."));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest req,
                                           HttpServletRequest request) {
        User performer = requireLeader(request);
        adminUseCase.resetPassword(id, req.newPassword(), performer.getId());
        return ResponseEntity.ok(Map.of("message", "비밀번호가 초기화되었습니다."));
    }

    @PostMapping("/users/{id}/toggle-otp")
    public ResponseEntity<?> toggleOtp(@PathVariable Long id, @RequestBody Map<String, String> body,
                                       HttpServletRequest request) {
        User performer = requireLeader(request);
        String otpEnabled = body.get("otpEnabled");
        if ("사용".equals(otpEnabled)) {
            adminUseCase.enableOtp(id, performer.getId());
        } else {
            adminUseCase.disableOtp(id, performer.getId());
        }
        return ResponseEntity.ok(Map.of("message", "OTP 설정이 변경되었습니다."));
    }

    @PostMapping("/users/{id}/reset-otp")
    public ResponseEntity<?> resetOtp(@PathVariable Long id, HttpServletRequest request) {
        User performer = requireLeader(request);
        adminUseCase.resetOtp(id, performer.getId());
        return ResponseEntity.ok(Map.of("message", "OTP가 초기화되었습니다."));
    }

    // ── Teams ──

    @GetMapping("/teams")
    public ResponseEntity<?> listTeams(HttpServletRequest request) {
        requireLeader(request);
        List<TeamResponse> teams = adminUseCase.findAllTeams().stream().map(TeamResponse::from).toList();
        return ResponseEntity.ok(teams);
    }

    @PostMapping("/teams")
    public ResponseEntity<?> createTeam(@RequestBody TeamCreateRequest req, HttpServletRequest request) {
        User performer = requireLeader(request);
        var team = adminUseCase.createTeam(req.division(), req.department(), req.name(), performer.getId());
        return ResponseEntity.ok(TeamResponse.from(team));
    }

    @PutMapping("/teams/{id}")
    public ResponseEntity<?> updateTeam(@PathVariable Long id, @RequestBody TeamUpdateRequest req,
                                        HttpServletRequest request) {
        User performer = requireLeader(request);
        adminUseCase.updateTeam(id, req.division(), req.department(), req.name(), performer.getId());
        var team = adminUseCase.findTeamById(id).orElseThrow();
        return ResponseEntity.ok(TeamResponse.from(team));
    }

    @DeleteMapping("/teams/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id, HttpServletRequest request) {
        User performer = requireLeader(request);
        adminUseCase.deleteTeam(id, performer.getId());
        return ResponseEntity.ok(Map.of("message", "부서가 삭제되었습니다."));
    }

    // ── Logs ──

    @GetMapping("/logs")
    public ResponseEntity<?> listLogs(HttpServletRequest request) {
        requireLeader(request);
        List<AuditLogResponse> logs = adminUseCase.findAllAuditLogs().stream().map(AuditLogResponse::from).toList();
        return ResponseEntity.ok(logs);
    }

    private User requireLeader(HttpServletRequest request) {
        User user = (User) request.getAttribute("loginUser");
        if (user == null || !user.isLeader()) {
            throw new SecurityException("관리자 권한이 필요합니다.");
        }
        return user;
    }
}
