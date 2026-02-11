package com.example.vite_coding_boot.adapter.in.web;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.vite_coding_boot.application.port.in.AssignmentUseCase;
import com.example.vite_coding_boot.application.port.in.UserQueryUseCase;
import com.example.vite_coding_boot.domain.model.Assignment;
import com.example.vite_coding_boot.domain.model.User;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/assignments")
public class AssignmentController {

    private final AssignmentUseCase assignmentUseCase;
    private final UserQueryUseCase userQueryUseCase;

    public AssignmentController(AssignmentUseCase assignmentUseCase, UserQueryUseCase userQueryUseCase) {
        this.assignmentUseCase = assignmentUseCase;
        this.userQueryUseCase = userQueryUseCase;
    }

    @GetMapping("/new")
    public String newAssignmentForm() {
        return "assignment-form";
    }

    @PostMapping
    public String createAssignment(@RequestParam String title,
                                   @RequestParam String description,
                                   @RequestParam LocalDate startDate,
                                   @RequestParam LocalDate dueDate,
                                   HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        assignmentUseCase.createAssignment(title, description, loginUser.getId(), startDate, dueDate);
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/delete")
    public String deleteAssignment(@PathVariable Long id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (!loginUser.isLeader()) {
            return "redirect:/dashboard";
        }
        assignmentUseCase.deleteAssignment(id);
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/approve")
    public String approveAssignment(@PathVariable Long id,
                                    @RequestParam Long assigneeUserId,
                                    HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (!loginUser.isLeader()) {
            return "redirect:/dashboard";
        }
        assignmentUseCase.approveAssignment(id, assigneeUserId);
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/reject")
    public String rejectAssignment(@PathVariable Long id,
                                   @RequestParam String rejectionReason,
                                   HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (!loginUser.isLeader()) {
            return "redirect:/dashboard";
        }
        assignmentUseCase.rejectAssignment(id, rejectionReason);
        return "redirect:/dashboard";
    }

    @GetMapping("/{id}/edit")
    public String editAssignmentForm(@PathVariable Long id, Model model) {
        Assignment assignment = assignmentUseCase.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다: " + id));
        model.addAttribute("assignment", assignment);
        return "assignment-edit-form";
    }

    @PostMapping("/{id}/edit")
    public String updateAssignment(@PathVariable Long id,
                                   @RequestParam String title,
                                   @RequestParam String description,
                                   @RequestParam LocalDate startDate,
                                   @RequestParam LocalDate dueDate) {
        assignmentUseCase.updateAssignment(id, title, description, startDate, dueDate);
        return "redirect:/dashboard";
    }

    @GetMapping("/{id}/result")
    public String resultForm(@PathVariable Long id, Model model, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        Assignment assignment = assignmentUseCase.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과제를 찾을 수 없습니다: " + id));
        if (assignment.getUser() == null || !assignment.getUser().getId().equals(loginUser.getId())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("assignment", assignment);
        return "assignment-result-form";
    }

    @PostMapping("/{id}/result")
    public String submitFinalResult(@PathVariable Long id,
                                    @RequestParam String finalResult,
                                    HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        assignmentUseCase.submitFinalResult(id, finalResult, loginUser.getId());
        return "redirect:/dashboard";
    }
}
