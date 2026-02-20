package com.example.vite_coding_boot.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vite_coding_boot.domain.model.Team;

public interface JpaTeamRepository extends JpaRepository<Team, Long> {
}
