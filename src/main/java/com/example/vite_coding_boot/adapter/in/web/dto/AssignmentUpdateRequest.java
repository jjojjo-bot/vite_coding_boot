package com.example.vite_coding_boot.adapter.in.web.dto;

import java.time.LocalDate;

public record AssignmentUpdateRequest(String title, String description, LocalDate startDate, LocalDate dueDate) {
}
