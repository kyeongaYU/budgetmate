package com.budgetmate.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.budgetmate.security.TokenParser;
import com.budgetmate.service.StatisService;

import reactor.core.publisher.Mono;


@RequestMapping("/statis")
@RestController
public class StatisController {

	private final TokenParser tokenParser;
	private final StatisService statisService;

	public StatisController(TokenParser tokenParser, StatisService statisService) {
		
		this.tokenParser = tokenParser;
		this.statisService = statisService;
		
	}
	
	@GetMapping("/getReceipt/calCurrentWeek")
	public ResponseEntity<Long> getCurrentWeekController(@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.replace("Bearer ", "").trim();
		Long userId = tokenParser.getUserIdFromToken(token);

		Long currentWeek = statisService.getCurrentWeekService(userId);
		return ResponseEntity.ok(currentWeek);
		
	}
	
	@GetMapping("/getReceipt/calKeywordTotalPrice")
	public ResponseEntity<Map<String, Integer>> getCalKeywordTotalPrice(@RequestHeader("Authorization") String authHeader) {
		
		String token = authHeader.replace("Bearer ", "").trim();
		Long userId = tokenParser.getUserIdFromToken(token);
		System.out.println("statisContriller - calKeywordTotalPrice 실행 / 유저 아이디 : " + userId);
		
		Map<String, Integer> result = statisService.calKeywordTotalPrice(userId);
		return ResponseEntity.ok(result);
				
		
	}

}
