package com.venkat.aichatbot.data.querying.Repository;

import com.venkat.aichatbot.data.querying.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
