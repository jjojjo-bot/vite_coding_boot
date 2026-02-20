package com.example.vite_coding_boot.adapter.in.web.dto;

import java.time.LocalDate;

public record AssignmentCreateRequest(String title, String description, LocalDate startDate, LocalDate dueDate) {
}
