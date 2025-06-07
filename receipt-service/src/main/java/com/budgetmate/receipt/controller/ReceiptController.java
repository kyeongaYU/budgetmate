package com.budgetmate.receipt.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.budgetmate.receipt.dto.ReceiptDto;
import com.budgetmate.receipt.service.ReceiptService;

import java.util.List;


@RestController
@RequestMapping("/receipt")
public class ReceiptController {

	private final ReceiptService receiptService;

	public ReceiptController(ReceiptService receiptService) {

		this.receiptService = receiptService;

	}

	@PostMapping("/createReceipt")
	public ReceiptDto createReceipt(@RequestBody ReceiptDto receiptDto) {
		receiptService.createReceipt(receiptDto);
		return receiptDto;
	}

	@GetMapping("/ledger")
	public List<ReceiptDto> getLedger(@RequestParam("userId") Long userId) {
		return receiptService.getReceiptsByUserId(userId);
	}

	@DeleteMapping("/{receiptId}")
	public ResponseEntity<Void> deleteReceipt(@PathVariable Long receiptId) {
		receiptService.deleteReceipt(receiptId);
		return ResponseEntity.noContent().build();
	}


}
