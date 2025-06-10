package com.budgetmate.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.budgetmate.dto.UserDto;

import lombok.RequiredArgsConstructor;

import java.time.*;
import java.time.temporal.*;

// JdbcTemplate : sql 실행을 간편하게 도와주는 spring의 도구 클래스 (jdbc 연결, 예외처리, preparedStatement 등을 내부적으로 처리)

@Repository		//스프링이 이 클래스를 데이터 접근 계층으로 인식하도록 함, 스프링이 자동으로 빈에 등록해줌.
@RequiredArgsConstructor
public class StatisQuery {
	
	private final JdbcTemplate jdbcTemplate;		// sql을 직접 실행할 수 있는 객체 (스프링이 제공)
	LocalDate today = LocalDate.now();
	LocalDate monday = today.with(DayOfWeek.MONDAY); // .with(): 특정 기준에 맞게 날짜를 조정함.
	// today는 클래스가 아니라 LocalDate클래스의 객체(변수 이름), 내부적으로는 TemporlAdjusters.previousOrSame(DayOfWeek.MONDAY(가 자동으로 적용.
	// temporalAdusters는 클래스 : temporalAduster를 생성해주는 정적메서드를 모아둔 유틸 클래스
	LocalDate now = LocalDate.now();

	
	public Long getCurrentWeek(Long userId) {
		System.out.println("5");
	    LocalDate today = LocalDate.now();
	    LocalDate monday = today.with(DayOfWeek.MONDAY);
	    String sql = "SELECT SUM(total_price) FROM receipt WHERE user_id = ? AND `date` BETWEEN ? AND ?";
	 // queryForObject() : sql을 실행하고 결과가 단 하나의 값일 때 사용.
	    Long totalPrice = jdbcTemplate.queryForObject(sql, Long.class, userId, java.sql.Date.valueOf(monday), java.sql.Date.valueOf(today)); // 챗 gpt가 monday를 java.sql...으로 감싸보라고 함.
	    
	    System.out.println("🧾 userId: " + userId);
	    System.out.println("🗓️ monday: " + monday);
	    System.out.println("🗓️ today: " + today);
	    System.out.println("🔢 totalPrice: " + totalPrice);
	    
	    return totalPrice != null ? totalPrice : 0L;
	}

	
	
	public Map<String, Integer> getKeywordTotalPrice(Long userId) {
	    Map<String, Integer> keywordTotal = new HashMap<>();
	    // String 별로 Integer를 저장하는 맵 구조.
	    String[] keywordNames = {"food", "living", "fashion", "health", "investment", "transportation"};
	    String sql = "SELECT SUM(total_price) FROM receipt WHERE user_id = ? AND keyword_id = ? AND `date` BETWEEN ? AND ?";

	    for (int i = 0; i < 6; i++) {
	        Integer price = jdbcTemplate.queryForObject(sql, Integer.class, userId, i + 1, monday, now);
	        keywordTotal.put(keywordNames[i], (price != null) ? price : 0);
	    }

	    return keywordTotal;
	}
	
	public List<UserDto> getUserList() {
		
		System.out.println("3 : getUserList 실행 시작 - 유저 목록 반환");
		String sql = "select * from user";
		List<UserDto> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserDto.class));
// BeanPropertyRowMapper : jdbcTemplate에서 거의 필수적임 : ResultSet의 각 행을 자바 객체로 자동으로 매핑해주는 도우미 클래스.
// 자바 객체 UserDto로 만들어주는 역할
// User.class를 넘기는 이유는 ResultSet을 User라는 클래스에 매핑할것이라고 jdbcTemplate에게 알려줌 -> user.class를 넘기면 내부적으로 reflection을 사용해서 setter를 자동으로 실행.
		return users;
		
	}
	
	public void updateUser(int lastWeek, int point, Long id) {
		
		System.out.println("updateUser 실행 - 유저 포인트, lastWeek 업데이트 시작");
		jdbcTemplate.update("update `user` set point = ?, last_week = ? where id = ?", point, lastWeek, id);
		
	}
	
	public boolean searchBadgeHistory(Long id, Long badgeId) {
		
		System.out.println("searchBadgeHistory 실행 시작 - 유저의 뱃지 히스토리 내역을 찾기 시작");
		String sql = "SELECT 1 FROM history WHERE user_id = ? AND badge_id = ? LIMIT 1"; // 일치하는 행이 하나라도 있으면 숫자 1을 가져오고 즉시 종료.
		//select 1은 컬럼이나 속성을 가져오는게 아니라 db의 존재 여부만 확인하거나 테스트용으로 1이라는 상수값만 리턴한다는 뜻 => 칼럼이아니라 1만 그냥 반환하고 쿼리를 끝냄.
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, id, badgeId); // queryForList : 결과가 List<Map<String, Object>> 형태로 반환됨.
		return !result.isEmpty();
// Object 값 : 정수, 문자열, 날짜 등 컬럼 타입에 따라 다름.
// 참고 : Count(*) 타입은 무조건 정수값으로 리턴되기 때문에 그냥 처음부터 boolean값으로 받을 수 없음.
	}
	
	public void updateHistory(Long id, Long badgeId) {
		
		System.out.println("updateHistory - History 테이블 업데이트 시작");
		//history : badge_id, user_id, week_start_date, granted_date
		String sql = "insert into history (badge_id, user_id, week_start_date, granted_date) values (?,?,?,?)";
		//String badgeSql = "update user set badge = ? where id = ?";
		// jdbcTemplate에서는 update() -> insert, update, delete 모두 수행됨.
		jdbcTemplate.update(sql, badgeId, id, monday, today);

		//jdbcTemplate.update(badgeSql, badge, id);
		
	}
	
	
	
	
	
	
// LocalDate : 클래스:java.time.LocalDate : 날짜를 표현하는 클래스, year-month-day
// DayOfWeek.MONDAY : java.time.DayOfWeek
	
// jdbcTemplate 주요 메서드 차이
// query() : List<T>를 반환함 / 예시 List<UserDto> : 여러 행을 특정 객체로 매핑할 때
// queryForList() : List<Map<String, Object>> / List<T> / 예시 List<String> : 1개 칼럼만 뽑고 싶을때 혹은 칼럼이 많지만 map<>형태로 받고 싶을때
// queryForObject : T(단일결과 1행만 반환) / 예시 String, Integer ... : 결과가 정확히 1행 1열이어야 함.
	
	

	
	
//	public Map<String, Integer> getKeywordTotalPrice(Long userId) {
//		
//		Map<String, Integer> keywordTotal = new HashMap<>();
//		Integer[] keywordPrice = new Integer[6];
//		int price = 0;
//
//		System.out.println("getKeywordTotkaPrice : statisQuery 로그 출력 !");
//		String sql = "select sum(total_price) from reciept where user_id = ? and keyword_id = ? group by keyword_id = ?";
//		
//		for (Long i = 0L; i < 6; i++) {
//			
//			price = jdbcTemplate.queryForObject(sql,  Long.class, userId, i + 1, i+ 1);
//			if (price == 0) keywordPrice[i.intValue()] = 0;
//			else {
//				keywordPrice[i.intValue()] = price;
//			}
//			
//		}
//		
//		keywordTotal.put("food", keywordPrice[1]);
//		keywordTotal.put("living", keywordPrice[2]);
//		keywordTotal.put("fashion", keywordPrice[3]);
//		keywordTotal.put("health", keywordPrice[4]);
//		keywordTotal.put("investment", keywordPrice[5]);
//		keywordTotal.put("transportation", keywordPrice[6]);
//		
//		return keywordTotal;
//		
//	}
	
}
