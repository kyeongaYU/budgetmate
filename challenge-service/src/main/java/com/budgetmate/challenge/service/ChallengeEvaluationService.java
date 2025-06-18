package com.budgetmate.challenge.service;

import com.budgetmate.challenge.client.BudgetClient;
import com.budgetmate.challenge.client.StatisClient;
import com.budgetmate.challenge.client.UserClient;
import com.budgetmate.challenge.entity.ChallengeEntity;
import com.budgetmate.challenge.enums.ChallengeType;
import com.budgetmate.challenge.repository.ChallengeRepository;
import com.budgetmate.challenge.util.TokenParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeEvaluationService {

    private final ChallengeRepository challengeRepository;
    private final StatisClient statisClient;
    private final BudgetClient budgetClient;
    private final UserClient userClient;
    private final TokenParser tokenParser;

    private final int REWARD_POINT = 2; // 챌린지 성공 시 지급 포인트

    // 기존 평가 방식 (userId만 이용)
    @Transactional
    public void evaluateChallenge(ChallengeEntity challenge) {
        Long userId = challenge.getUserId();
        evaluate(challenge, userId, null);
    }

    // 헤더 기반 평가 방식 (JWT 토큰에서 userId 파싱)
    @Transactional
    public void evaluateChallenge(ChallengeEntity challenge, String authHeader) {
        Long userId = tokenParser.getUserIdFromToken(authHeader);
        evaluate(challenge, userId, authHeader);
    }

    // 핵심 평가 로직 (내부 호출용)
    private void evaluate(ChallengeEntity challenge, Long userId, String authHeader) {
        LocalDate start = challenge.getStartDate();
        LocalDate end = challenge.getEndDate();

        boolean success = false;

        switch (challenge.getType()) {
            case NO_SPENDING -> {
                int totalSpent = (authHeader != null)
                        ? statisClient.getTotalSpent(authHeader)
                        : statisClient.getTotalSpent(userId, start, end);
                log.info("NO_SPENDING ▶ 총 소비액: {}", totalSpent);
                success = totalSpent <= challenge.getTargetAmount();
            }

            case CATEGORY_LIMIT -> {
                Map<String, Integer> categoryMap = (authHeader != null)
                        ? statisClient.getCategorySpent(authHeader)
                        : statisClient.getCategorySpent(userId, start, end);
                int spent = categoryMap.getOrDefault(challenge.getTargetCategory(), 0);
                log.info("CATEGORY_LIMIT ▶ {} 소비액: {}", challenge.getTargetCategory(), spent);
                success = spent <= challenge.getTargetAmount();
            }

            case SAVING -> {
                int budget = budgetClient.getMonthlyBudget(userId, start.getYear(), start.getMonthValue());
                int actualSpent = (authHeader != null)
                        ? statisClient.getMonthlySpent(authHeader)
                        : statisClient.getMonthlySpent(userId, start.getYear(), start.getMonthValue());

                if (budget == 0) {
                    log.warn("SAVING ▶ 예산이 0원입니다. 평가 불가 (userId: {})", userId);
                    break;
                }

                double savingRate = ((double) (budget - actualSpent) / budget) * 100;
                log.info("SAVING ▶ 예산: {}, 소비액: {}, 절약률: {}%", budget, actualSpent, savingRate);

                success = savingRate >= 10.0;
            }
        }

        if (success) {
            userClient.addPoint(userId, REWARD_POINT);
            challenge.setSuccess(true);
            log.info("🎉 챌린지 성공! 포인트 {}점 지급 완료 (userId: {})", REWARD_POINT, userId);
        } else {
            log.info("😢 챌린지 실패 (userId: {})", userId);
        }

        challengeRepository.save(challenge);
    }
}
