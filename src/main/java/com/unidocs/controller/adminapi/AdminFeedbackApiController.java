package com.unidocs.controller.adminapi;

import com.unidocs.domain.Feedback;
import com.unidocs.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/admin/api/feedback")
public class AdminFeedbackApiController {

    private final FeedbackService feedbackService;

    public AdminFeedbackApiController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        return feedbackService.createEmitter();
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<?> replyFeedback(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String replyContent = payload.get("replyContent");
            Feedback feedback = feedbackService.replyToFeedback(id, replyContent);
            return ResponseEntity.ok(Map.of("message", "Phản hồi đã được gửi", "feedback", feedback));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Có lỗi xảy ra khi phản hồi"));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        try {
            feedbackService.deleteFeedback(id);
            return ResponseEntity.ok(Map.of("message", "Góp ý đã được xóa"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Có lỗi xảy ra khi xóa góp ý"));
        }
    }
}
