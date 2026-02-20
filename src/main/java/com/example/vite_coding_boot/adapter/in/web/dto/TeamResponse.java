package com.example.vite_coding_boot.adapter.in.web.dto;

import com.example.vite_coding_boot.domain.model.Team;

public record TeamResponse(Long id, String division, String department, String name, String fullName) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(team.getId(), team.getDivision(), team.getDepartment(), team.getName(), team.getFullName());
    }
}
