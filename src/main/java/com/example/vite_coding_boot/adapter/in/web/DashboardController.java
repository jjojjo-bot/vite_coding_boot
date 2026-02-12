package com.example.vite_coding_boot.adapter.in.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.vite_coding_boot.application.port.in.AssignmentUseCase;
import com.example.vite_coding_boot.application.port.in.UserQueryUseCase;
import com.example.vite_coding_boot.domain.model.Assignment;
import com.example.vite_coding_boot.domain.model.Role;
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
        model.addAttribute("currentPage", "dashboard");

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

    @GetMapping("/approval-history")
    public String approvalHistory(HttpSession session, Model model,
                                  @RequestParam(required = false) String status) {
        User loginUser = (User) session.getAttribute("loginUser");
        model.addAttribute("user", loginUser);
        model.addAttribute("currentPage", "approval-history");
        model.addAttribute("currentStatus", status);

        List<Assignment> assignments = assignmentUseCase.findAllAssignments();
        if (status != null && !status.isEmpty()) {
            assignments = assignments.stream()
                    .filter(a -> a.getApprovalStatus().name().equals(status))
                    .toList();
        }
        model.addAttribute("assignments", assignments);
        if (loginUser.isLeader()) {
            model.addAttribute("members", userQueryUseCase.findAllMembers());
        }

        return "approval-history";
    }

    @GetMapping("/users/new")
    public String userAddForm(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (!loginUser.isLeader()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", loginUser);
        model.addAttribute("currentPage", "user-add");

        return "user-add";
    }

    @PostMapping("/users")
    public String createUser(HttpSession session,
                             @RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String name,
                             @RequestParam Role role,
                             RedirectAttributes redirectAttributes) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (!loginUser.isLeader()) {
            return "redirect:/dashboard";
        }
        try {
            userQueryUseCase.createUser(username, password, name, role);
            redirectAttributes.addFlashAttribute("successMessage", "사용자가 성공적으로 추가되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users/new";
    }
}
