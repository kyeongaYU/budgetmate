package com.budgetmate.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class OcrResultDto {
    private String shopName;      // 점포명
    private LocalDate date;       // 결제일
    private int totalPrice;       // 총금액
    private String imagePath;     // 서버에 저장된 이미지 경로

    // 기존에 쓰던 3개 인자 생성자 추가
    public OcrResultDto(String shopName, LocalDate date, int totalPrice) {
        this.shopName = shopName;
        this.date = date;
        this.totalPrice = totalPrice;
        this.imagePath = "";      // imagePath는 빈 문자열로 초기화
    }

    // 새로 추가한 4개 인자 생성자
    public OcrResultDto(String shopName, LocalDate date, int totalPrice, String imagePath) {
        this.shopName = shopName;
        this.date = date;
        this.totalPrice = totalPrice;
        this.imagePath = imagePath;
    }
}
