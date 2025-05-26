package com.budgetmate.receipt.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiptDto {
	
	private Long receiptId;
	private String shop;
	private Long userId;
	private LocalDate date;
	private Long keywordId;
	
	public ReceiptDto() {}
	
	public ReceiptDto(String shop, Long userId, LocalDate date, Long keywordId) { //영수증 등록 dto
		
		this.shop = shop;
		this.userId = userId;
		this.date = date;
		this.keywordId = keywordId;
		
	}

}
