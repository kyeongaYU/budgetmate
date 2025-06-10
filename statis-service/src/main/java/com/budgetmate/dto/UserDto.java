package com.budgetmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
	
	private Long Id;
	private int point;
	private int lastWeek;
	private int currentWeek;
	//private int badge;
	// BeanPropertyRowMapper : 스네이크 <-> 카멜 케이스 변환을 자동 지원함.
	
	public void addPoint() {
		System.out.println("7 : addPoint");
		point++;
	}
	

}
