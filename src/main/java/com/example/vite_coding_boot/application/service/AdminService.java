package com.example.vite_coding_boot.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.vite_coding_boot.application.port.in.AdminUseCase;
import com.example.vite_coding_boot.application.port.in.OtpUseCase;
import com.example.vite_coding_boot.application.port.out.AuditLogRepository;
import com.example.vite_coding_boot.application.port.out.TeamRepository;
import com.example.vite_coding_boot.application.port.out.UserRepository;
import com.example.vite_coding_boot.domain.model.AuditAction;
import com.example.vite_coding_boot.domain.model.AuditLog;
import com.example.vite_coding_boot.domain.model.AuditTargetType;
import com.example.vite_coding_boot.domain.model.Role;
import com.example.vite_coding_boot.domain.model.Team;
import com.example.vite_coding_boot.domain.model.User;

@Service
@Transactional
public class AdminService implements AdminUseCase {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final AuditLogRepository auditLogRepository;
    private final OtpUseCase otpUseCase;

    public AdminService(UserRepository userRepository, TeamRepository teamRepository,
                        AuditLogRepository auditLogRepository, OtpUseCase otpUseCase) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.auditLogRepository = auditLogRepository;
        this.otpUseCase = otpUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void updateUser(Long userId, String name, String role, Long teamId, Long performerUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        User performer = userRepository.findById(performerUserId)
                .orElseThrow(() -> new IllegalArgumentException("수행자를 찾을 수 없습니다: " + performerUserId));

        user.setName(name);
        user.setRole(Role.valueOf(role));
        if (teamId != null) {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다: " + teamId));
            user.setTeam(team);
        } else {
            user.setTeam(null);
        }
        userRepository.save(user);

        auditLogRepository.save(new AuditLog(AuditAction.UPDATE, AuditTargetType.USER, userId,
                "사용자 정보 수정: " + user.getUsername(), performer));
    }

    @Override
    public void deleteUser(Long userId, Long performerUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        User performer = userRepository.findById(performerUserId)
                .orElseThrow(() -> new IllegalArgumentException("수행자를 찾을 수 없습니다: " + performerUserId));

        String username = user.getUsername();
        userRepository.deleteById(userId);

        auditLogRepository.save(new AuditLog(AuditAction.DELETE, AuditTargetType.USER, userId,
                "사용자 삭제: " + username, performer));
    }

    @Override
    public void resetPassword(Long userId, String newPassword, Long performerUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        User performer = userRepository.findById(performerUserId)
                .orElseThrow(() -> new IllegalArgumentException("수행자를 찾을 수 없습니다: " + performerUserId));

        user.setPassword(newPassword);
        userRepository.save(user);

        auditLogRepository.save(new AuditLog(AuditAction.UPDATE, AuditTargetType.USER, userId,
                "비밀번호 초기화: " + user.getUsername(), performer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> findAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Team> findTeamById(Long id) {
        return teamRepository.findById(id);
    }

    @Override
    public Team createTeam(String division, String department, String name, Long performerUserId) {
        User performer = userRepository.findById(performerUserId)
                .orElseThrow(() -> new IllegalArgumentException("수행자를 찾을 수 없습니다: " + performerUserId));

        Team team = teamRepository.save(new Team(division, department, name));

        auditLogRepository.save(new AuditLog(AuditAction.CREATE, AuditTargetType.TEAM, team.getId(),
                "부서 생성: " + team.getFullName(), performer));
        return team;
    }

    @Override
    public void updateTeam(Long teamId, String division, String department, String name, Long performerUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다: " + teamId));
        User performer = userRepository.findById(performerUserId)
                .orElseThrow(() -> new IllegalArgumentException("수행자를 찾을 수 없습니다: " + performerUserId));

        team.setDivision(division);
        team.setDepartment(department);
        team.setName(name);
        teamRepository.save(team);

        auditLogRepository.save(new AuditLog(AuditAction.UPDATE, AuditTargetType.TEAM, teamId,
                "부서 수정: " + team.getFullName(), performer));
    }

    @Override
    public void deleteTeam(Long teamId, Long performerUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다: " + teamId));
        User performer = userRepository.findById(performerUserId)
                .orElseThrow(() -> new IllegalArgumentException("수행자를 찾을 수 없습니다: " + performerUserId));

        List<User> usersInTeam = userRepository.findByTeamId(teamId);
        if (!usersInTeam.isEmpty()) {
            throw new IllegalStateException("소속 사용자가 있는 부서는 삭제할 수 없습니다. (" + usersInTeam.size() + "명)");
        }

        String fullName = team.getFullName();
        teamRepository.deleteById(teamId);

        auditLogRepository.save(new AuditLog(AuditAction.DELETE, AuditTargetType.TEAM, teamId,
                "부서 삭제: " + fullName, performer));
    }

    @Override
    public void enableOtp(Long userId, Long performerUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        User performer = userRepository.findById(performerUserId)
                .orElseThrow(() -> new IllegalArgumentException("수행자를 찾을 수 없습니다: " + performerUserId));

        String secret = otpUseCase.generateSecret();
        user.setOtpSecret(secret);
        user.setOtpResetRequired(true);
        userRepository.save(user);

        auditLogRepository.save(new AuditLog(AuditAction.UPDATE, AuditTargetType.USER, userId,
                "OTP 활성화: " + user.getUsername(), performer));
    }

    @Override
    public void disableOtp(Long userId, Long performerUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        User performer = userRepository.findById(performerUserId)
                .orElseThrow(() -> new IllegalArgumentException("수행자를 찾을 수 없습니다: " + performerUserId));

        user.setOtpSecret(null);
        user.setOtpResetRequired(false);
        userRepository.save(user);

        auditLogRepository.save(new AuditLog(AuditAction.UPDATE, AuditTargetType.USER, userId,
                "OTP 비활성화: " + user.getUsername(), performer));
    }

    @Override
    public String resetOtp(Long userId, Long performerUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        User performer = userRepository.findById(performerUserId)
                .orElseThrow(() -> new IllegalArgumentException("수행자를 찾을 수 없습니다: " + performerUserId));

        String newSecret = otpUseCase.generateSecret();
        user.setOtpSecret(newSecret);
        user.setOtpResetRequired(true);
        userRepository.save(user);

        auditLogRepository.save(new AuditLog(AuditAction.UPDATE, AuditTargetType.USER, userId,
                "OTP 초기화: " + user.getUsername(), performer));
        return newSecret;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> findAllAuditLogs() {
        return auditLogRepository.findAll();
    }
}
