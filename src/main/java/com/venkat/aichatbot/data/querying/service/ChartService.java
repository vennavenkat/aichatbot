package com.venkat.aichatbot.data.querying.service;

import com.venkat.aichatbot.data.querying.dto.ChartRequest;
import lombok.RequiredArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChartService {

    public String generateChartImage(ChartRequest request) {
        List<String> labels = request.getLabels();
        List<Number> values = request.getValues();

        if (labels == null || values == null || labels.size() != values.size()) {
            throw new IllegalArgumentException("Labels and values must be non-null and of equal size.");
        }

        JFreeChart chart;
        switch (request.getChartType().toLowerCase()) {
            case "line":
                chart = createLineChart(labels, values);
                break;
            case "pie":
                chart = createPieChart(labels, values);
                break;
            case "bar":
            default:
                chart = createBarChart(labels, values);
        }

        BufferedImage image = chart.createBufferedImage(600, 400);
        return encodeImageToBase64(image);
    }

    private JFreeChart createBarChart(List<String> labels, List<Number> values) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < labels.size(); i++) {
            dataset.addValue(values.get(i), "Value", labels.get(i));
        }
        return ChartFactory.createBarChart("Bar Chart", "Category", "Value", dataset);
    }

    private JFreeChart createLineChart(List<String> labels, List<Number> values) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < labels.size(); i++) {
            dataset.addValue(values.get(i), "Value", labels.get(i));
        }
        return ChartFactory.createLineChart("Line Chart", "Category", "Value", dataset);
    }

    private JFreeChart createPieChart(List<String> labels, List<Number> values) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (int i = 0; i < labels.size(); i++) {
            dataset.setValue(labels.get(i), values.get(i));
        }
        return ChartFactory.createPieChart("Pie Chart", dataset, true, true, false);
    }

    private String encodeImageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode chart image", e);
        }
    }
}
