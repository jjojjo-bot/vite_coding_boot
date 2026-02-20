package com.example.vite_coding_boot.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {"/", "/login", "/otp-setup", "/dashboard", "/approval-history",
            "/assignments/**", "/admin/**", "/otp-settings"})
    public String forward() {
        return "forward:/index.html";
    }
}
