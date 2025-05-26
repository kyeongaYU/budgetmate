package com.budgetmate.receipt.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class OcrRequestDto {
    private MultipartFile image;
}
