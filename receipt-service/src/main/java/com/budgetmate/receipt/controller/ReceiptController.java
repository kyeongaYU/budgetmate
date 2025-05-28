package com.budgetmate.receipt.controller;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.*;

import com.budgetmate.receipt.dto.ReceiptDto;
import com.budgetmate.receipt.service.ReceiptService;

@RestController
@RequestMapping("receipt")
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


}
