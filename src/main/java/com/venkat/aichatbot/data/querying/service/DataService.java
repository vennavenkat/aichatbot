package com.venkat.aichatbot.data.querying.service;


import com.venkat.aichatbot.data.querying.Repository.DataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DataService {

    private final DataRepository dataRepository;

    public List<Map<String, Object>> getFirstNRows(String tableName, int limit) {
        List<Object[]> rawRows = dataRepository.fetchTableData(tableName, limit);
        List<String> columnNames = dataRepository.fetchColumnNames(tableName);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rawRows) {
            Map<String, Object> rowMap = new LinkedHashMap<>();
            for (int i = 0; i < columnNames.size(); i++) {
                rowMap.put(columnNames.get(i), row[i]);
            }
            result.add(rowMap);
        }

        return result;
    }
//    public List<Map<String, String>> readSampleData(String fileName, int limit) {
//        if (fileName.endsWith(".csv")) {
//            return dataRepository.readCsvSample(fileName, limit);
//        } else if (fileName.endsWith(".xlsx")) {
//            return dataRepository.readExcelSample(fileName, limit);
//        } else {
//            throw new IllegalArgumentException("Unsupported file type: " + fileName);
//        }
//    }
}

