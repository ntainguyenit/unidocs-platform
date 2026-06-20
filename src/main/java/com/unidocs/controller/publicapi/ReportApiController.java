package com.unidocs.controller.publicapi;

import com.unidocs.dto.ReportDto;
import com.unidocs.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportApiController {

    private final ReportService reportService;

    public ReportApiController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> submitReport(@RequestBody ReportDto reportDto) {
        try {
            reportService.submitReport(reportDto);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cảm ơn bạn đã báo cáo. Chúng tôi sẽ xử lý sớm nhất có thể!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Lỗi gửi báo cáo: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
