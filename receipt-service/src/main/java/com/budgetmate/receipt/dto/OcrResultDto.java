package com.budgetmate.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrResultDto {

    private String shopName;      // 점포명
    private LocalDate date;       // 결제일
    private int totalPrice;       // 총금액
}
