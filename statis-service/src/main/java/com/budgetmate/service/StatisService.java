package com.budgetmate.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.budgetmate.query.StatisQuery;

import reactor.core.publisher.Mono;

@Service
public class StatisService {
	
	private final StatisQuery statisQuery;
	
	public StatisService(StatisQuery statisQuery) {
		
		this.statisQuery = statisQuery;
		
	}
	
	public Long getCurrentWeekService(Long userId) {
	
		
		System.out.println("StatisService - getCurrentWeekService 실행 / 유저아이디 : " + userId);
		return statisQuery.getCurrentWeek(userId);
		
	}
	
	public Map<String, Integer> calKeywordTotalPrice(Long userId) {
		
		System.out.println("statisService - calKeywordTotalPrice 실행 / 유저 아이디 : " + userId);
		
		Map<String, Integer> keywordTotal =  statisQuery.getKeywordTotalPrice(userId);
		return keywordTotal;
		
	}
	
//	private final WebClient webClient;
//	
//	public StatisService(WebClient webClient) {
//		
//		this.webClient = webClient;
//		
//	}
//	
//	public Mono<ResponseEntity<Long>> getCurrentWeek(Long userId) {
//		
//		return 
//				webClient.get()
//					.uri("http://localhost:8082/reciept/getReciept")
//					.retrieve()	// 요청을 보내고 응답을 받아오는 트리거
//					//.bodyToMono(Long.class)	// 리턴 타입을 Mono<T>로 감쌈 (리액티브 객체)
//					// .block(); 	// .block() 은 동기방식으로 받음. 안붙이면 비동기로 작동
//					//.subscribe();   // 실제 실행을 트리거 하기만 함. 겨로가를 사용할 수는 없음. 리턴 타입은 void임.
//					.toEntity(Long.class); // 응답 전체(상태코드, 헤더, 바디)를 가져옴.
//		
//	}
	
	

}

//WebClient.get()
//.uri("reciept/getReciept")
//.retrieve()	// 요청을 보내고 응답을 받아오는 트리거
//.bodyToMono(String.class)	// 리턴 타입을 Mono<T>로 감쌈 (리액티브 객체)
////.block(); 	// .block() 은 동기방식으로 받음. 안붙이면 비동기로 작동
//.subscribe();   // 실제 실행을 위한구독 처리
//.bodyToMono() : http 응답 본문 body만 가져옴 : 리턴타입  Mono<Long>
//.toEntity() : 응답 전체 (상태코드, 헤더, 바디)를 가져옴.    Mono<ResponseEntity<Long>>