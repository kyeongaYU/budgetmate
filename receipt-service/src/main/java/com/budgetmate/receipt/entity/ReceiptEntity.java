package com.budgetmate.receipt.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name="receipt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long receiptId;

	@Column(name = "shop", length = 50)
	private String shop;

	@Column(name = "image_path", nullable = true, length = 255)
	private String imagePath;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "keyword_id", nullable = false)
	private Long keywordId;

	@Column(name = "total_price", nullable = false)
	private Long totalPrice;

	@Column(name = "date", nullable = false)
	@Builder.Default
	private LocalDate date = LocalDate.now();

	@Column(name = "is_deleted", nullable = false)
	@Builder.Default
	private Boolean isDeleted = false;
}
