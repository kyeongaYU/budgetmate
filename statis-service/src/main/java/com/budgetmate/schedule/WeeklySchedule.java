package com.budgetmate.schedule;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budgetmate.badgeTypeEnum.BadgeType;
import com.budgetmate.dto.UserDto;
import com.budgetmate.query.StatisQuery;



@Component // Schedule 사용할 클래스는 반드시 빈으로 등록되어야 스프링이 인식함.
public class WeeklySchedule {
	
	private final StatisQuery statisQuery; 
	private BadgeType badgeType; // 이미 만들어놓은 싱글턴 객체. 변수로 보관해서 재사용하고 싶으면 필드 선언이 필요함.
	
//	public enum BadgeType { // 클래스처럼 작동하는 enum 타입.
//		
//		Beginner(1L, 10), // BadgeType.BEGINNER는 문자열이 안리ㅏ BadgeType의 객체 중 하나. 
//		INTERMEDIATE(2L, 30), // 각각의 객체
//		MASTER(3L, 50); // 각각의 객체
//		
//		private final Long badgeId;
//		private final int requeiredPoint;
//		
//		BadgeType(Long badgeId, int requiredPoint) {
//			this.badgeId = badgeId;
//			this.requeiredPoint = requiredPoint;
//		}
//	
//		public Long getId() {
//			return badgeId;
//		}
//
//	}
	
	public WeeklySchedule(StatisQuery statisQuery) {
		
		this.statisQuery = statisQuery;
		//this.badgeType = badgeType; // Beginner, .., master 중 뭐가 들어올까? (각각은 badgeType의 객체임)
		// 단지 badgeType 타입의 참조 필드일 뿐인거고 어떤 값(beginner,,)가 들어올지는 생성할 때 정해짐.
		// 셋 중 하나의 객체를 넘겨준다고 함,, 사실 완벽한 이해는 되지 않았음..
	}
	
	@Transactional
	//@Scheduled(cron = "0 55 23 ? * SUN")
	@Scheduled(cron = "0 25 19 ? * SAT")
	public void WeeklyScheduleUpdate() { // CRON의 표현식 : 초 분 시 일 월 요일
		
//		System.out.println("1: WeeklyScheduleUpdate 실행 시작");
//		updatePoint();
//		updateBadge();
//		System.out.println("WeeklyScheduleupdate 실행 완료");
		
		// 유저 목록 조회
        // 이번주 소비 계산
        // 지난주와 비교 → 포인트 증가 여부 판단
        // lastWeek 업데이트
        // DB에 저장
		System.out.println("2 : updatePoint 실행 시작");
		List<UserDto> users = statisQuery.getUserList(); // 유저 목록 조회.
		
		for (UserDto user : users) {
			System.out.println("4");
			int currentWeek = statisQuery.getCurrentWeek(user.getId()).intValue(); // 이번주 소비 계산
			System.out.println("6");
			if (currentWeek < user.getLastWeek()) { //만약 조건 충족
				System.out.println("조건 충족 ");
				int point = user.getPoint() + 1;
				user.setPoint(point);
				//int badge = user.getBadge();
				
				//if(++point < 10) continue;
				if (point >= 10) {

				if(point == 10) {  // 절약 초보 뱃지
					System.out.println(user.getId() + " 를 검사하는 중...");
					badgeType = BadgeType.Beginner;
					//if (!statisQuery.searchBadgeHistory(user.getId(), 1L)){//(없다면) 만약 이전에 받은 기록이 없다면 부여 아니면 말음.
					if (!statisQuery.searchBadgeHistory(user.getId(), badgeType.getId())) {
						// history 넣어야 할것 : 발급일자 granted_date, 뱃지 아이디 = 1, 유저 아이디,시작 일자 (week_start_date)
						//updateHistory() : 여기서 history를 업데이트
						System.out.println("뱃지 발급 하기 ======= ");
						statisQuery.updateHistory(user.getId(),badgeType.getId());
						
					}
					
				} else if (point == 30) {
					badgeType = BadgeType.INTERMEDIATE;
					if (!statisQuery.searchBadgeHistory(user.getId(), badgeType.getId())) {
						System.out.println("뱃지 발급 하기 ======= ");
						statisQuery.updateHistory(user.getId(), badgeType.getId());

					}
					
				} else if (point == 50) {
					badgeType = BadgeType.MASTER;
					if (!statisQuery.searchBadgeHistory(user.getId(), badgeType.getId())) {
						System.out.println("뱃지 발급 하기 ======= ");
						statisQuery.updateHistory(user.getId(), badgeType.getId());
					}

					}
				}
				
			}
			// 아닐 때 + 현재 값을 저번주 소비로 집어넣어야 함.
			//user.setLastWeek(currentWeek);
			
			// 업데이트
			statisQuery.updateUser(currentWeek, user.getPoint(),  user.getId());

		}
		
				
	}
	
//	@Transactional
//	public void updatePoint() {
//		
//		// 유저 목록 조회
//        // 이번주 소비 계산
//        // 지난주와 비교 → 포인트 증가 여부 판단
//        // lastWeek 업데이트
//        // DB에 저장
//		System.out.println("2 : updatePoint 실행 시작");
//		List<UserDto> users = statisQuery.getUserList(); // 유저 목록 조회.
//		
//		for (UserDto user : users) {
//			System.out.println("4");
//			int currentWeek = statisQuery.getCurrentWeek(user.getId()).intValue(); // 이번주 소비 계산
//			System.out.println("6");
//			if (currentWeek < user.getLastWeek()) { //만약 조건 충
//				
//				user.addPoint();
//				
//			}
//			// 아닐 때 + 현재 값을 저번주 소비로 집어넣어야 함.
//			user.setLastWeek(currentWeek);
//			
//			// 업데이트
//			statisQuery.updateUser(currentWeek, user.getPoint(), user.getId());
//
//		}
//		
//	}
	
//	@Transactional
//	public void updateBadge() {// 절약초보(10) -> 프로절약러(30) -> 절약(60)
//		
//		System.out.println("updateBadge 실행 시작");
//		
//		// 유저 목록 조회
//        // 포인트 조건 비교
//        // badge 부여 여부 판단
//        // DB에 저장
//		System.out.println("updateBadge 함수 실행");
//		List<UserDto> users = statisQuery.getUserList();  // 유저 목록 조회.
//		for (UserDto user : users) {
//			System.out.println("user아이디" + user.getId() + "badge : " + user.getBadge());
			// 이미 뱃지를 받았다면 그 다음에 받을 수 없도록 해야 함.
//			if(user.getPoint() < 10) continue;
//			int point = user.getPoint() + 1;
//			user.setPoint(point);
//			
//			int badge = user.getBadge();
//			System.out.println("뱃지 개수 : " + badge);
//			if(user.getPoint() == 10) {  // 절약 초보 뱃지
//				System.out.println(user.getId() + " 를 검사하는 중...");
//				badgeType = BadgeType.Beginner;
//				//if (!statisQuery.searchBadgeHistory(user.getId(), 1L)){//(없다면) 만약 이전에 받은 기록이 없다면 부여 아니면 말음.
//				if (!statisQuery.searchBadgeHistory(user.getId(), badgeType.getId())) {
//					// history 넣어야 할것 : 발급일자 granted_date, 뱃지 아이디 = 1, 유저 아이디,시작 일자 (week_start_date)
//					//updateHistory() : 여기서 history를 업데이트
//					//++badge;
//					user.setBadge(++badge);
//					System.out.println("업데이트 뱃지 개수 : " + badge);
//					statisQuery.updateHistory(user.getId(),badgeType.getId(), badge);
//					
//				}
//				
//			} else if (user.getPoint() == 30) {
//				badgeType = BadgeType.INTERMEDIATE;
//				if (!statisQuery.searchBadgeHistory(user.getId(), badgeType.getId())) {
//					++badge;
//					System.out.println("업데이트 뱃지 개수 : " + badge);
//					statisQuery.updateHistory(user.getId(), badgeType.getId(),badge);
//
//				}
//				
//			} else if (user.getPoint() == 50) {
//				badgeType = BadgeType.MASTER;
//				if (!statisQuery.searchBadgeHistory(user.getId(), badgeType.getId())) {
//					++badge;
//					System.out.println("업데이트 뱃지 개수 : " + badge);
//					statisQuery.updateHistory(user.getId(), badgeType.getId(),badge);
//
//				}
//			}
//			
//		}
//		
//	}

}
