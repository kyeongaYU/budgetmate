package com.budgetmate.receipt.controller;

import com.budgetmate.receipt.dto.OcrResultDto;
import com.budgetmate.receipt.service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/receipt")
public class OcrController {

    private final OcrService ocrService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/ocr")
    public ResponseEntity<OcrResultDto> analyzeByPath(@RequestParam("path") String relativePath) {
        String filename = Paths.get(relativePath).getFileName().toString();
        String fullPath = Paths.get(uploadDir, filename).toString();

        try {
            OcrResultDto dto = ocrService.analyzeReceiptFromFile(fullPath);
            dto.setImagePath(relativePath);
            return ResponseEntity.ok(dto);
        } catch (IOException e) {
            log.error("OCR 처리 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}