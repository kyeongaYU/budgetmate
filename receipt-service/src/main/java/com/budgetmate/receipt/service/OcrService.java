package com.budgetmate.receipt.service;

import com.budgetmate.receipt.dto.OcrResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrService {

    @Value("${naver.ocr.invoke-url}")
    private String invokeUrl;

    @Value("${naver.ocr.secret-key}")
    private String secretKey;

    @Value("${naver.ocr.access-key}")
    private String accessKey;

    public Mono<OcrResultDto> analyzeReceipt(FilePart imagePart) {
        return DataBufferUtils.join(imagePart.content())
                .flatMap(dataBuffer -> {
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer); // 메모리 해제

                        String base64Image = Base64.getEncoder().encodeToString(bytes);

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

                        WebClient webClient = WebClient.builder()
                                .baseUrl(invokeUrl)
                                .defaultHeader("X-OCR-SECRET", secretKey)
                                .defaultHeader("Authorization", accessKey)
                                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .build();

                        return webClient.post()
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .map(response -> {
                                    try {
                                        Map<String, Object> imageResult = (Map<String, Object>) ((List<?>) response.get("images")).get(0);
                                        Map<String, Object> receipt = (Map<String, Object>) imageResult.get("receipt");
                                        Map<String, Object> result = (Map<String, Object>) receipt.get("result");

                                        String shopName = Optional.ofNullable(result)
                                                .map(r -> (Map<String, Object>) r.get("storeInfo"))
                                                .map(s -> (Map<String, Object>) s.get("name"))
                                                .map(n -> (Map<String, Object>) n.get("formatted"))
                                                .map(f -> (String) f.get("value"))
                                                .orElse("상호명 없음");

                                        LocalDate date = Optional.ofNullable(result)
                                                .map(r -> (Map<String, Object>) r.get("paymentInfo"))
                                                .map(p -> (Map<String, Object>) p.get("date"))
                                                .map(d -> (Map<String, Object>) d.get("formatted"))
                                                .map(f -> {
                                                    try {
                                                        return LocalDate.of(
                                                                Integer.parseInt((String) f.get("year")),
                                                                Integer.parseInt((String) f.get("month")),
                                                                Integer.parseInt((String) f.get("day"))
                                                        );
                                                    } catch (Exception e) {
                                                        return LocalDate.now();
                                                    }
                                                }).orElse(LocalDate.now());

                                        int totalPrice = Optional.ofNullable(result)
                                                .map(r -> (Map<String, Object>) r.get("totalPrice"))
                                                .map(p -> (Map<String, Object>) p.get("price"))
                                                .map(f -> (Map<String, Object>) f.get("formatted"))
                                                .map(m -> {
                                                    try {
                                                        return Integer.parseInt(((String) m.get("value")).replaceAll("[^0-9]", ""));
                                                    } catch (Exception e) {
                                                        return 0;
                                                    }
                                                }).orElse(0);

                                        return new OcrResultDto(shopName, date, totalPrice);
                                    } catch (Exception e) {
                                        log.error("OCR 파싱 실패", e);
                                        return new OcrResultDto("인식 실패", LocalDate.now(), 0);
                                    }
                                });
                    } catch (Exception e) {
                        DataBufferUtils.release(dataBuffer); // 예외 발생 시에도 메모리 해제
                        log.error("DataBuffer read 실패", e);
                        return Mono.just(new OcrResultDto("인식 실패", LocalDate.now(), 0));
                    }
                })
                .onErrorResume(e -> {
                    log.error("OCR 처리 중 예외 발생", e);
                    return Mono.just(new OcrResultDto("인식 실패", LocalDate.now(), 0));
                });
    }
}