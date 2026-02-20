package com.example.vite_coding_boot.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.vite_coding_boot.application.port.out.TeamRepository;
import com.example.vite_coding_boot.domain.model.Team;

@Component
public class TeamPersistenceAdapter implements TeamRepository {

    private final JpaTeamRepository jpaTeamRepository;

    public TeamPersistenceAdapter(JpaTeamRepository jpaTeamRepository) {
        this.jpaTeamRepository = jpaTeamRepository;
    }

    @Override
    public Team save(Team team) {
        return jpaTeamRepository.save(team);
    }

    @Override
    public List<Team> findAll() {
        return jpaTeamRepository.findAll();
    }

    @Override
    public Optional<Team> findById(Long id) {
        return jpaTeamRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaTeamRepository.deleteById(id);
    }
}
