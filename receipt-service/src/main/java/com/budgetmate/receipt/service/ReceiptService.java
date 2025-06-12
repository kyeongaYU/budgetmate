package com.budgetmate.receipt.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.budgetmate.receipt.dto.ReceiptDto;
import com.budgetmate.receipt.entity.ReceiptEntity;
import com.budgetmate.receipt.repository.ReceiptRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReceiptService {

	private final ReceiptRepository receiptRepository;

	@Autowired
	public ReceiptService(ReceiptRepository receiptRepository) {
		this.receiptRepository = receiptRepository;
	}

	public void createReceipt(ReceiptDto dto) {
		ReceiptEntity receipt = ReceiptEntity.builder()
				.shop(dto.getShop())
				.imagePath(dto.getImagePath())
				.userId(dto.getUserId())
				.date(dto.getDate())
				.keywordId(dto.getKeywordId())
				.totalPrice(dto.getTotalPrice())
				.build();

		receiptRepository.save(receipt);
	}
	public List<ReceiptDto> getReceiptsByUserId(Long userId) {
		return receiptRepository.findByUserIdAndIsDeletedFalse(userId).stream()
				.map(entity -> ReceiptDto.builder()
						.receiptId(entity.getReceiptId())
						.shop(entity.getShop())
						.userId(entity.getUserId())
						.imagePath(entity.getImagePath())
						.date(entity.getDate())
						.keywordId(entity.getKeywordId())
						.totalPrice(entity.getTotalPrice())
						.build())
				.collect(Collectors.toList());
	}

	@Transactional
	public void deleteReceipt(Long receiptId) {
		ReceiptEntity receipt = receiptRepository.findById(receiptId)
				.orElseThrow(() -> new IllegalArgumentException("해당 영수증이 없습니다."));
		receipt.setIsDeleted(true); // ← 소프트 삭제
	}

}

