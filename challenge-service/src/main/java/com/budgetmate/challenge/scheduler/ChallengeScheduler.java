package com.budgetmate.challenge.scheduler;

import com.budgetmate.challenge.entity.ChallengeEntity;
import com.budgetmate.challenge.repository.ChallengeRepository;
import com.budgetmate.challenge.service.ChallengeEvaluationService;
import com.budgetmate.challenge.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeScheduler {

    private final ChallengeRepository challengeRepository;
    private final ChallengeEvaluationService challengeEvaluationService;
    private final ChallengeService challengeService;

    // 매일 새벽 2시에 만료된 챌린지 평가
    @Scheduled(cron = "0 8 20 * * *")  // 매일 새벽 2시
    public void evaluateAllExpiredChallenges() {
        log.info("🔄 만료된 챌린지 평가 시작");

        List<ChallengeEntity> expiredChallenges = challengeService.getExpiredUnevaluatedChallenges();

        for (ChallengeEntity challenge : expiredChallenges) {
            challengeEvaluationService.evaluateChallengeInternal(challenge);
        }

        log.info("✅ 만료된 챌린지 평가 완료: {}건", expiredChallenges.size());
    }

}
