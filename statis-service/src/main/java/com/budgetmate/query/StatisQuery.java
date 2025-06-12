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

@Repository
@RequiredArgsConstructor
public class StatisQuery {
	
	private final JdbcTemplate jdbcTemplate;
	LocalDate today = LocalDate.now();
	LocalDate monday = today.with(DayOfWeek.MONDAY);
	LocalDate now = LocalDate.now();


	public Long getCurrentWeek(Long userId) {
		LocalDate today = LocalDate.now();
		LocalDate monday = today.with(DayOfWeek.MONDAY);
		String sql = "SELECT SUM(total_price) FROM receipt WHERE user_id = ? AND keyword_id != ? AND `date` BETWEEN ? AND ?";

		Long totalPrice = jdbcTemplate.queryForObject(sql, Long.class, userId, 8, java.sql.Date.valueOf(monday), java.sql.Date.valueOf(today)); // 챗 gpt가 monday를 java.sql...으로 감싸보라고 함.

		System.out.println("🧾 userId: " + userId);
		System.out.println("🗓️ monday: " + monday);
		System.out.println("🗓️ today: " + today);
		System.out.println("🔢 totalPrice: " + totalPrice);

		return totalPrice != null ? totalPrice : 0L;
	}

	
	
	public Map<String, Integer> getKeywordTotalPrice(Long userId) {
	    Map<String, Integer> keywordTotal = new HashMap<>();

	    String[] keywordNames = {"food", "transportation", "living", "fashion", "health", "education", "investment"};
	    String sql = "SELECT SUM(total_price) FROM receipt WHERE user_id = ? AND keyword_id = ? AND `date` BETWEEN ? AND ?";

	    for (int i = 0; i < 7; i++) {
	        Integer price = jdbcTemplate.queryForObject(sql, Integer.class, userId, i + 1, monday, now);
	        keywordTotal.put(keywordNames[i], (price != null) ? price : 0);
	    }
		System.out.println("== getKeywordTotalPrice 실행 ===");
		System.out.println("Living keyword total price: " + keywordTotal.get("living"));
		System.out.println("transportation keyword total price: " + keywordTotal.get("living"));
		System.out.println("food keyword total price : " + keywordTotal.get("food"));



		return keywordTotal;
	}
	
	public List<UserDto> getUserList() {
		
		System.out.println("3 : getUserList 실행 시작 - 유저 목록 반환");
		String sql = "select * from user";
		List<UserDto> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(UserDto.class));
		return users;
		
	}
	
	public void updateUser(int lastWeek, int point, Long id) {
		
		System.out.println("updateUser 실행 - 유저 포인트, lastWeek 업데이트 시작");
		jdbcTemplate.update("update `user` set point = ?, last_week = ? where id = ?", point, lastWeek, id);
		
	}
	
	public boolean searchBadgeHistory(Long id, Long badgeId) {
		
		System.out.println("searchBadgeHistory 실행 시작 - 유저의 뱃지 히스토리 내역을 찾기 시작");
		String sql = "SELECT 1 FROM history WHERE user_id = ? AND badge_id = ? LIMIT 1";
		List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, id, badgeId);
		return !result.isEmpty();
	}
	
	public void updateHistory(Long id, Long badgeId) {
		
		System.out.println("updateHistory - History 테이블 업데이트 시작");

		String sql = "insert into history (badge_id, user_id, week_start_date, granted_date) values (?,?,?,?)";

		jdbcTemplate.update(sql, badgeId, id, monday, today);

	}
	
	
	}
