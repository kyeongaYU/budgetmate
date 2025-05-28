package com.budgetmate.receipt.controller;

import com.budgetmate.receipt.dto.OcrResultDto;
import com.budgetmate.receipt.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/receipt")
public class OcrController {

    private final OcrService ocrService;

    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<OcrResultDto>> analyzeReceipt(@RequestPart("image") Mono<FilePart> imageMono) {
        return imageMono.flatMap(image -> {
            System.out.println("수신한 파일: " + image.filename());
            return ocrService.analyzeReceipt(image)
                    .map(ResponseEntity::ok);
        });
    }
}
