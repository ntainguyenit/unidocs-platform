package com.unidocs.controller.publicapi;

import com.unidocs.service.VisitorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final VisitorService visitorService;

    public StatsController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Long>> ping(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "false") boolean newVisitor) {
        
        visitorService.ping(sessionId, newVisitor);
        
        Map<String, Long> response = new HashMap<>();
        response.put("online", visitorService.getOnlineCount());
        response.put("total", visitorService.getTotalVisitors());
        
        return ResponseEntity.ok(response);
    }
}
