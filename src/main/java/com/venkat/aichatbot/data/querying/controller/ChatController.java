package com.venkat.aichatbot.data.querying.controller;

import com.venkat.aichatbot.data.querying.Repository.ChatLogRepository;
import com.venkat.aichatbot.data.querying.dto.*;
import com.venkat.aichatbot.data.querying.entity.ChatLog;
import com.venkat.aichatbot.data.querying.service.ChartService;
import com.venkat.aichatbot.data.querying.service.ChatService;
import com.venkat.aichatbot.data.querying.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChartService chartService;
    private final FeedbackService feedbackService;
    private final ChatLogRepository chatLogRepository;


    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@RequestBody AskRequest request) {
        AskResponse response = chatService.processQuestion(request.getQuestion());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chart")
    public ResponseEntity<ChartResponse> generateChart(@RequestBody ChartRequest request) {
        String base64Image = chartService.generateChartImage(request);
        return ResponseEntity.ok(new ChartResponse(base64Image));
    }

    @PostMapping("/feedback")
    public ResponseEntity<String> submitFeedback(@RequestBody FeedbackRequest request) {
        feedbackService.saveFeedback(request);
        return ResponseEntity.ok("Feedback received. Thank you!");
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ChatLogDTO>> getRecentLogs() {
        List<ChatLog> logs = chatLogRepository.findTop20ByOrderByTimestampDesc();
        List<ChatLogDTO> dtos = logs.stream()
                .map(log -> new ChatLogDTO(log.getQuestion(), log.getGeneratedSql(), log.getResponse(), log.getTimestamp()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/health")
    public ResponseEntity<HealthStatus> checkHealth() {
        boolean dbStatus = chatService.checkDatabase();
        boolean llmStatus = chatService.checkLLM();

        String message = (dbStatus && llmStatus) ? "All systems operational" :
                (!dbStatus ? "Database down" : "LLM API unreachable");

        return ResponseEntity.ok(new HealthStatus(dbStatus, llmStatus, message));
    }



}
