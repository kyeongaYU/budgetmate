package com.budgetmate.receipt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.budgetmate.receipt.dto.ReceiptDto;
import com.budgetmate.receipt.entity.ReceiptEntity;
import com.budgetmate.receipt.repository.ReceiptRepository;

@Service
public class ReceiptService {

	@Autowired
	private ReceiptRepository receiptRepository;
	
	public ReceiptService(ReceiptRepository receiptRepository) {
		
		this.receiptRepository = receiptRepository;
		
	}
	
	public void createReceipt (ReceiptDto receiptDto) {
		
		ReceiptEntity receipt = ReceiptEntity.builder()
				.shop(receiptDto.getShop())
				.userId(receiptDto.getUserId())
				.date(receiptDto.getDate())
				.keywordId(receiptDto.getKeywordId())
				.build();
		
		receiptRepository.save(receipt);
		
	}
	
}
