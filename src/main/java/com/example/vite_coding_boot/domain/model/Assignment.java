package com.example.vite_coding_boot.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    private String rejectionReason;

    @Column(length = 2000)
    private String finalResult;

    private LocalDateTime resultRegisteredAt;

    protected Assignment() {
    }

    public Assignment(String title, String description, User createdBy, LocalDate startDate, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.startDate = startDate;
        this.dueDate = dueDate;
    }

    public ProgressStatus calculateProgressStatus() {
        if (approvalStatus != ApprovalStatus.APPROVED) {
            return null;
        }
        if (finalResult != null) {
            if (!resultRegisteredAt.toLocalDate().isAfter(dueDate.plusDays(5))) {
                return ProgressStatus.COMPLETED;
            }
            return ProgressStatus.DELAYED_COMPLETED;
        }
        if (LocalDate.now().isAfter(dueDate.plusDays(5))) {
            return ProgressStatus.DELAYED;
        }
        if (!LocalDate.now().isBefore(startDate)) {
            return ProgressStatus.IN_PROGRESS;
        }
        return ProgressStatus.NOT_STARTED;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(String finalResult) {
        this.finalResult = finalResult;
    }

    public LocalDateTime getResultRegisteredAt() {
        return resultRegisteredAt;
    }

    public void setResultRegisteredAt(LocalDateTime resultRegisteredAt) {
        this.resultRegisteredAt = resultRegisteredAt;
    }
}
