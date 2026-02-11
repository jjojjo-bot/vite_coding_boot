package com.example.vite_coding_boot.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AssignmentTest {

    private Assignment createApprovedAssignment(LocalDate startDate, LocalDate dueDate) {
        Assignment assignment = new Assignment("과제", "설명", null, startDate, dueDate);
        assignment.setApprovalStatus(ApprovalStatus.APPROVED);
        return assignment;
    }

    @Test
    void notApproved_returnsNull() {
        Assignment assignment = new Assignment("과제", "설명", null, LocalDate.now(), LocalDate.of(2099, 12, 31));

        assertNull(assignment.calculateProgressStatus());
    }

    @Test
    void rejectedAssignment_returnsNull() {
        Assignment assignment = new Assignment("과제", "설명", null, LocalDate.now(), LocalDate.of(2099, 12, 31));
        assignment.setApprovalStatus(ApprovalStatus.REJECTED);

        assertNull(assignment.calculateProgressStatus());
    }

    @Test
    void completedOnTime_returnsCompleted() {
        LocalDate dueDate = LocalDate.of(2099, 12, 31);
        Assignment assignment = createApprovedAssignment(LocalDate.of(2099, 1, 1), dueDate);
        assignment.setFinalResult("완료");
        assignment.setResultRegisteredAt(dueDate.atStartOfDay());

        assertEquals(ProgressStatus.COMPLETED, assignment.calculateProgressStatus());
    }

    @Test
    void completedWithinGracePeriod_returnsCompleted() {
        LocalDate dueDate = LocalDate.of(2099, 6, 15);
        Assignment assignment = createApprovedAssignment(LocalDate.of(2099, 1, 1), dueDate);
        assignment.setFinalResult("완료");
        assignment.setResultRegisteredAt(dueDate.plusDays(5).atStartOfDay());

        assertEquals(ProgressStatus.COMPLETED, assignment.calculateProgressStatus());
    }

    @Test
    void completedAfterGracePeriod_returnsDelayedCompleted() {
        LocalDate dueDate = LocalDate.of(2025, 1, 1);
        Assignment assignment = createApprovedAssignment(LocalDate.of(2024, 12, 1), dueDate);
        assignment.setFinalResult("완료");
        assignment.setResultRegisteredAt(dueDate.plusDays(6).atStartOfDay());

        assertEquals(ProgressStatus.DELAYED_COMPLETED, assignment.calculateProgressStatus());
    }

    @Test
    void pastGracePeriodAndNoResult_returnsDelayed() {
        LocalDate dueDate = LocalDate.of(2020, 1, 1);
        Assignment assignment = createApprovedAssignment(LocalDate.of(2019, 12, 1), dueDate);

        assertEquals(ProgressStatus.DELAYED, assignment.calculateProgressStatus());
    }

    @Test
    void afterStartDate_returnsInProgress() {
        Assignment assignment = createApprovedAssignment(LocalDate.now().minusDays(1), LocalDate.of(2099, 12, 31));

        assertEquals(ProgressStatus.IN_PROGRESS, assignment.calculateProgressStatus());
    }

    @Test
    void onStartDate_returnsInProgress() {
        Assignment assignment = createApprovedAssignment(LocalDate.now(), LocalDate.of(2099, 12, 31));

        assertEquals(ProgressStatus.IN_PROGRESS, assignment.calculateProgressStatus());
    }

    @Test
    void beforeStartDate_returnsNotStarted() {
        Assignment assignment = createApprovedAssignment(LocalDate.of(2099, 1, 1), LocalDate.of(2099, 12, 31));

        assertEquals(ProgressStatus.NOT_STARTED, assignment.calculateProgressStatus());
    }
}
