package com.venkat.aichatbot.data.querying.service;

import com.venkat.aichatbot.data.querying.Repository.FeedbackRepository;
import com.venkat.aichatbot.data.querying.dto.FeedbackRequest;
import com.venkat.aichatbot.data.querying.entity.Feedback;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository repository;

    public void saveFeedback(FeedbackRequest request) {
        Feedback feedback = new Feedback(
                null,
                request.isThumbsUp(),
                request.getComments(),
                request.getQuestion(),
                request.getGeneratedSql(),
                request.getResponseSummary(),
                LocalDateTime.now()
        );
        repository.save(feedback);
    }
}
