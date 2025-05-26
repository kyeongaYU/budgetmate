package com.budgetmate.receipt.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="receipt")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ReceiptEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long receiptId;
	
	@Column(name = "shop", length = 50)
	private String shop;
	
	@Column(name = "userId", nullable = false)
	private Long userId;
	
	@Column(name = "keyword_id")
	private Long keywordId;
	
	@Builder.Default
	@Column(name = "date", nullable = false)
	private LocalDate date = LocalDate.now();
	
	@Builder.Default
	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted = false;
	
	public ReceiptEntity() {}
	
	/*
	@Builder  //내부적으로 모든 필드를 받는 생성자를 자동 호출하기 때문
	public ReceiptEntity(String shop, Long userId, Long keywordId, LocalDate date, Boolean isDeleted) {
		
		this.shop = shop;
		this.userId = userId;
		this.keywordId = keywordId;
		this.date = date;
		this.isDeleted = isDeleted;
		
	}
	*/
	
}
