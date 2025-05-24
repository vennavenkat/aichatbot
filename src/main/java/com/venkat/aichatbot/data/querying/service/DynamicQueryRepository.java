package com.venkat.aichatbot.data.querying.service;

import java.util.List;
import java.util.Map;

public interface DynamicQueryRepository {
    List<Map<String, Object>> executeDynamicSql(String sql);
}
