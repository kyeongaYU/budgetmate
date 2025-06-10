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
	
	//private final WebClient webClient;
	//private final CalCurrentWeek calCurrentWeek;
	//private final BringReceipt bringReciept;
	private final TokenParser tokenParser;
	private final StatisService statisService;
	
//	public StatisController(StatisService statisService, WebClient webClient, CalCurrentWeek calCurrentWeek, BringReceipt bringReciept, TokenParser tokenParser) {
//		
//		this.webClient = webClient;
//		this.calCurrentWeek = calCurrentWeek;
//		this.bringReciept = bringReciept;
//		this.tokenParser = tokenParser;
//		this.statisService = statisService;
//		
//	}
	public StatisController(TokenParser tokenParser, StatisService statisService) {
		
		this.tokenParser = tokenParser;
		this.statisService = statisService;
		
	}
	
	@GetMapping("/getReceipt/calCurrentWeek")
	//@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
//	@CrossOrigin(
//		    origins = {"http://localhost:5173", "http://localhost:3000"},
//		    allowedHeaders = "*",
//		    exposedHeaders = "Authorization",
//		    allowCredentials = "true" // 필요하면
//		)
	public ResponseEntity<Long> getCurrentWeekController(@RequestHeader("Authorization") String authHeader) {

		System.out.println("=== 1. 이번주 소비 합계 : statis 컨트롤러 진입 ===");
		String token = authHeader.replace("Bearer ", "").trim();
		Long userId = tokenParser.getUserIdFromToken(token);
		System.out.println("=== 2. 이번주 소비 합계 : statis 컨트롤러 userId = " + userId + " token : " + token);
		//System.out.println("StatisController - getCurrentWeekController 실행 / 유저 아이디 : " + userId);
		Long currentWeek = statisService.getCurrentWeekService(userId);
		System.out.println("=== 3. 이번주 소비 합계 : 서비스 실행 완료 , currentWeek = " + currentWeek);
		
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
	
//	@GetMapping("/getReceipt/calCurretWeek")
//	public ResponseEntity<Long> currentWeekController (@RequestHeader("Authorization") String authHeader) {
//		
//		String token = authHeader.replace("Bearer ", "");
//		Long userId = tokenParser.getUserIdFromToken(token);
//		
//		return statisService.getCurrentWeek(userId);
//		
//	}
	
	

	
//	@GetMapping("/getReceipt/calCurrentWeek")
//	public Mono<ResponseEntity<Long>> currentWeekStatisController(@RequestHeader("Authorization") String authHeader) {
//		
//		String token = authHeader.replace("Bearer ", "");
//		Long userId = tokenParser.getUserIdFromToken(token);
//		//bringReciept.getReceipt(userId);
//
//		return statisService.getCurrentWeek(userId);
//				
//		
//	}

	

}
//WebClient.get()
//.uri("reciept/getReciept")
//.retrieve()	// 요청을 보내고 응답을 받아오는 트리거
//.bodyToMono(String.class)	// 리턴 타입을 Mono<T>로 감쌈 (리액티브 객체)
//// .block(); 	// .block() 은 동기방식으로 받음. 안붙이면 비동기로 작동
//.subscribe();   // 실제 실행을 위한구독 처리