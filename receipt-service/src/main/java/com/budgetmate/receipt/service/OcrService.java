package com.budgetmate.receipt.service;

import com.budgetmate.receipt.dto.OcrResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {

    private final RestTemplate restTemplate;

    @Value("${naver.ocr.invoke-url}")
    private String invokeUrl;
    @Value("${naver.ocr.secret-key}")
    private String secretKey;
    @Value("${naver.ocr.access-key}")
    private String accessKey;

    public OcrResultDto analyzeReceiptFromFile(String filePath) throws IOException {
        // 1) 파일 읽어서 Base64 인코딩
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        String base64Image = Base64.getEncoder().encodeToString(bytes);

        // 2) 요청 바디 구성
        Map<String,Object> imageMap = Map.of(
                "format","jpg",
                "name","receipt",
                "data", base64Image
        );
        Map<String,Object> requestBody = Map.of(
                "images", List.of(imageMap),
                "requestId", UUID.randomUUID().toString(),
                "version","V2",
                "timestamp", System.currentTimeMillis()
        );

        // 3) 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-OCR-SECRET", secretKey);
        headers.set("Authorization", accessKey);

        HttpEntity<Map<String,Object>> request = new HttpEntity<>(requestBody, headers);

        // 4) POST 요청 및 응답(Map) 받기
        @SuppressWarnings("unchecked")
        Map<String,Object> response =
                restTemplate.postForObject(invokeUrl, request, Map.class);

        // 5) 응답 파싱 (기존 로직 그대로)
        @SuppressWarnings("unchecked")
        Map<String,Object> imageResult = (Map<String,Object>) ((List<?>)response.get("images")).get(0);
        @SuppressWarnings("unchecked")
        Map<String,Object> receipt    = (Map<String,Object>) imageResult.get("receipt");
        @SuppressWarnings("unchecked")
        Map<String,Object> result     = (Map<String,Object>) receipt.get("result");

        String shopName = Optional.ofNullable(result)
                .map(r -> (Map<String,Object>)r.get("storeInfo"))
                .map(s -> (Map<String,Object>)s.get("name"))
                .map(n -> (Map<String,Object>)n.get("formatted"))
                .map(f -> (String)f.get("value"))
                .orElse("상호명 없음");

        LocalDate date = Optional.ofNullable(result)
                .map(r -> (Map<String,Object>)r.get("paymentInfo"))
                .map(p -> (Map<String,Object>)p.get("date"))
                .map(d -> (Map<String,Object>)d.get("formatted"))
                .map(f -> {
                    try {
                        return LocalDate.of(
                                Integer.parseInt((String)f.get("year")),
                                Integer.parseInt((String)f.get("month")),
                                Integer.parseInt((String)f.get("day"))
                        );
                    } catch (Exception ex) {
                        return LocalDate.now();
                    }
                })
                .orElse(LocalDate.now());

        int totalPrice = Optional.ofNullable(result)
                .map(r -> (Map<String,Object>)r.get("totalPrice"))
                .map(p -> (Map<String,Object>)p.get("price"))
                .map(f -> (Map<String,Object>)f.get("formatted"))
                .map(m -> {
                    try {
                        return Integer.parseInt(((String)m.get("value")).replaceAll("\\D",""));
                    } catch (Exception ex) {
                        return 0;
                    }
                })
                .orElse(0);

        // 6) DTO 생성 및 반환
        OcrResultDto dto = new OcrResultDto(shopName, date, totalPrice);
        String filename = Paths.get(filePath).getFileName().toString();
        dto.setImagePath(filename);
        return dto;
    }
}
