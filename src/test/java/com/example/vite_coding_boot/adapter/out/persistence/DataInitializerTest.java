package com.example.vite_coding_boot.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.vite_coding_boot.domain.model.ApprovalStatus;
import com.example.vite_coding_boot.domain.model.Role;
import com.example.vite_coding_boot.domain.model.User;

@SpringBootTest
class DataInitializerTest {

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private JpaAssignmentRepository assignmentRepository;

    @Test
    void initialDataLoaded() {
        assertEquals(13, userRepository.count());
    }

    @Test
    void assignmentsLoaded() {
        assertEquals(100, assignmentRepository.count());
    }

    @Test
    void leaderAccountExists() {
        User leader = userRepository.findByUsername("leader").orElseThrow();
        assertEquals("조장", leader.getName());
        assertEquals(Role.LEADER, leader.getRole());
        assertTrue(leader.isLeader());
        assertFalse(leader.isOtpEnabled());
    }

    @Test
    void memberAccountsExist() {
        assertTrue(userRepository.findByUsername("member1").isPresent());
        assertTrue(userRepository.findByUsername("member2").isPresent());

        User member1 = userRepository.findByUsername("member1").orElseThrow();
        assertEquals(Role.MEMBER, member1.getRole());
    }
}
