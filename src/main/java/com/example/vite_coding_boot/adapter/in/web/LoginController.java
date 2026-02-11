package com.example.vite_coding_boot.adapter.in.web;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.vite_coding_boot.application.port.in.UserQueryUseCase;
import com.example.vite_coding_boot.domain.model.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final UserQueryUseCase userQueryUseCase;

    public LoginController(UserQueryUseCase userQueryUseCase) {
        this.userQueryUseCase = userQueryUseCase;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("loginUser") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Optional<User> user = userQueryUseCase.authenticate(username, password);
        if (user.isPresent()) {
            session.setAttribute("loginUser", user.get());
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
