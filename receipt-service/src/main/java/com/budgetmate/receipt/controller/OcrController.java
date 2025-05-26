package com.budgetmate.receipt.controller;

import com.budgetmate.receipt.dto.OcrResultDto;
import com.budgetmate.receipt.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/receipt")
public class OcrController {

    private final OcrService ocrService;

    @PostMapping("/ocr")
    public ResponseEntity<OcrResultDto> analyzeReceipt(@RequestParam("image") MultipartFile image) throws Exception {
        OcrResultDto result = ocrService.analyzeReceipt(image);
        return ResponseEntity.ok(result);
    }
}
