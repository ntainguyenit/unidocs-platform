package com.unidocs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegalController {

    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("title", "Điều khoản sử dụng");
        return "terms";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("title", "Chính sách bảo mật");
        return "privacy";
    }
}