package com.foodDelivery.foodDeliveryCoursework.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads/menu_images/";

    public String saveFile(MultipartFile file) {
        try {
            // Создаем уникальное имя файла
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String filePath = UPLOAD_DIR + filename;

            // Создаем папку, если ее нет
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Сохраняем файл
            Files.copy(file.getInputStream(), Paths.get(filePath));

            return filePath;
        } catch (IOException e) {
            System.out.println("CANNOT UPLOAD FILE");
            throw new RuntimeException("Failed to save file", e);
        }
    }
}
