package com.venkat.aichatbot.data.querying.service;

import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.core.io.Resource;

@Service
public class FileUploadService {

    private static final String UPLOAD_DIR = "/Users/venkatkumarvenna/Downloads"; // Or use application.properties

    public void storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.endsWith(".csv") || fileName.endsWith(".xlsx"))) {
            throw new RuntimeException("Only CSV and Excel files are supported");
        }

        try {
            Path targetPath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not store file: " + fileName, e);
        }
    }



    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException("Error loading file: " + fileName, e);
        }
    }

}
