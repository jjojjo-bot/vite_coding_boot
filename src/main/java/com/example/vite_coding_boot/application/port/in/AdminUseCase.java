package com.example.vite_coding_boot.application.port.in;

import java.util.List;
import java.util.Optional;

import com.example.vite_coding_boot.domain.model.AuditLog;
import com.example.vite_coding_boot.domain.model.Team;
import com.example.vite_coding_boot.domain.model.User;

public interface AdminUseCase {

    List<User> findAllUsers();

    Optional<User> findUserById(Long id);

    void updateUser(Long userId, String name, String role, Long teamId, Long performerUserId);

    void deleteUser(Long userId, Long performerUserId);

    void resetPassword(Long userId, String newPassword, Long performerUserId);

    List<Team> findAllTeams();

    Optional<Team> findTeamById(Long id);

    Team createTeam(String division, String department, String name, Long performerUserId);

    void updateTeam(Long teamId, String division, String department, String name, Long performerUserId);

    void deleteTeam(Long teamId, Long performerUserId);

    void enableOtp(Long userId, Long performerUserId);

    void disableOtp(Long userId, Long performerUserId);

    String resetOtp(Long userId, Long performerUserId);

    List<AuditLog> findAllAuditLogs();
}
