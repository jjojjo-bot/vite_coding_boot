package com.example.vite_coding_boot.adapter.in.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.vite_coding_boot.application.port.in.AssignmentUseCase;
import com.example.vite_coding_boot.application.port.in.UserQueryUseCase;
import com.example.vite_coding_boot.domain.model.Assignment;
import com.example.vite_coding_boot.domain.model.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    private final AssignmentUseCase assignmentUseCase;
    private final UserQueryUseCase userQueryUseCase;

    public DashboardController(AssignmentUseCase assignmentUseCase, UserQueryUseCase userQueryUseCase) {
        this.assignmentUseCase = assignmentUseCase;
        this.userQueryUseCase = userQueryUseCase;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        model.addAttribute("user", loginUser);

        List<Assignment> assignments;
        if (loginUser.isLeader()) {
            assignments = assignmentUseCase.findAllAssignments();
        } else {
            assignments = assignmentUseCase.findAssignmentsByCreatorOrAssignee(loginUser);
        }
        model.addAttribute("assignments", assignments);
        model.addAttribute("members", userQueryUseCase.findAllMembers());

        return "dashboard";
    }
}
