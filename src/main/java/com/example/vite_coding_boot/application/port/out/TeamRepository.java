package com.example.vite_coding_boot.application.port.out;

import java.util.List;
import java.util.Optional;

import com.example.vite_coding_boot.domain.model.Team;

public interface TeamRepository {

    Team save(Team team);

    List<Team> findAll();

    Optional<Team> findById(Long id);

    void deleteById(Long id);
}
