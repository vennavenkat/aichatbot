package com.venkat.aichatbot.data.querying.Repository;

import com.venkat.aichatbot.data.querying.entity.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    List<ChatLog> findTop20ByOrderByTimestampDesc(); // Latest 20 logs
}
