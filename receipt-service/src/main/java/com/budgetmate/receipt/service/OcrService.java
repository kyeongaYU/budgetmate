package com.budgetmate.receipt.service;

import com.budgetmate.receipt.dto.OcrResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OcrService {

    @Value("${naver.ocr.invoke-url}")
    private String invokeUrl;

    @Value("${naver.ocr.secret-key}")
    private String secretKey;

    @Value("${naver.ocr.access-key}")
    private String accessKey;

    public OcrResultDto analyzeReceipt(MultipartFile image) throws Exception {
        try {
            //  이미지 Base64 인코딩
            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());

            //  JSON 요청 바디 구성
            Map<String, Object> imageMap = Map.of(
                    "format", "jpg",
                    "name", "receipt",
                    "data", base64Image
            );
            Map<String, Object> requestBody = Map.of(
                    "images", List.of(imageMap),
                    "requestId", UUID.randomUUID().toString(),
                    "version", "V2",
                    "timestamp", System.currentTimeMillis()
            );

            //  헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-OCR-SECRET", secretKey);
            headers.set("Authorization", accessKey);

            //  API 요청
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = new RestTemplate().postForEntity(invokeUrl, request, Map.class);

            // 응답 파싱
            Map<String, Object> body = response.getBody();
            Map<String, Object> imageResult = (Map<String, Object>) ((List<?>) body.get("images")).get(0);
            Map<String, Object> receipt = (Map<String, Object>) imageResult.get("receipt");
            Map<String, Object> result = (Map<String, Object>) receipt.get("result");

            //  상호명 추출
            String shopName = "상호명 없음";
            try {
                Map<String, Object> storeInfo = (Map<String, Object>) result.get("storeInfo");
                Map<String, Object> name = (Map<String, Object>) storeInfo.get("name");
                Map<String, Object> formatted = (Map<String, Object>) name.get("formatted");
                shopName = (String) formatted.get("value");
            } catch (Exception ignored) {}

            //  결제일 추출
            LocalDate date = LocalDate.now();
            try {
                Map<String, Object> paymentInfo = (Map<String, Object>) result.get("paymentInfo");
                Map<String, Object> dateMap = (Map<String, Object>) paymentInfo.get("date");
                Map<String, Object> formatted = (Map<String, Object>) dateMap.get("formatted");

                int year = Integer.parseInt((String) formatted.get("year"));
                int month = Integer.parseInt((String) formatted.get("month"));
                int day = Integer.parseInt((String) formatted.get("day"));

                date = LocalDate.of(year, month, day);
            } catch (Exception ignored) {}

            // 총금액 추출
            int totalPrice = 0;
            try {
                Map<String, Object> totalPriceMap = (Map<String, Object>) result.get("totalPrice");
                Map<String, Object> priceMap = (Map<String, Object>) totalPriceMap.get("price");
                Map<String, Object> formatted = (Map<String, Object>) priceMap.get("formatted");

                String priceStr = (String) formatted.get("value");
                totalPrice = Integer.parseInt(priceStr.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}

            // 9. DTO로 반환
            return new OcrResultDto(shopName, date, totalPrice);

        } catch (Exception e) {
            e.printStackTrace();
            return new OcrResultDto("인식 실패", LocalDate.now(), 0);
        }
    }
}
