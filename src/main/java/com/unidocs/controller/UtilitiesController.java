package com.unidocs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/utilities")
public class UtilitiesController {

    @GetMapping("/exam-simulation")
    public String examSimulation(Model model) {
        return "utilities/exam-simulation";
    }

    @GetMapping("/timetable")
    public String timetable(Model model) {
        return "utilities/timetable";
    }
}
