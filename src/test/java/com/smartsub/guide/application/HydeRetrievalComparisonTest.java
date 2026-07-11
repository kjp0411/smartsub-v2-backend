package com.smartsub.guide.application;

import com.smartsub.guide.domain.GuideDocumentRepository;
import com.smartsub.guide.domain.GuideDocumentScoredProjection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * HyDE 적용 전/후 검색 품질 비교 스크립트.
 * 실제 OpenAI 호출 + 실제 pgvector DB가 필요해서 기본은 @Disabled.
 *
 * 실행 방법:
 *   1) 아래 @Disabled 줄을 지운다
 *   2) application-test.yml 등에 실제 OpenAI API 키 + DB 연결 확인
 *   3) ./gradlew test --tests "*HydeRetrievalComparisonTest*"
 *   4) 콘솔 출력 표를 캡처해서 포트폴리오에 사용
 *   5) 확인 후 다시 @Disabled로 되돌려 평소 빌드에 안 끼게 한다
 */
@Disabled("실제 OpenAI 호출 + DB가 필요한 수동 평가용 테스트. 실행 시 이 줄을 지우세요.")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HydeRetrievalComparisonTest {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private HydeQueryExpander hydeQueryExpander;

    @Autowired
    private GuideDocumentRepository guideDocumentRepository;

    private UUID storeId;

    private record Case(String question, String expectedKeyword) {}

    private final List<Case> cases = List.of(
        new Case("여기 주차할 수 있어요?", "주차장"),
        new Case("화장실 어디예요?", "화장실"),
        new Case("쉬는 시간 있나요?", "브레이크타임"),
        new Case("할인 이벤트 있어요?", "할인"),
        new Case("몇 시까지 하나요?", "영업시간"),
        new Case("와이파이 비밀번호 알려주세요", "와이파이"),
        new Case("아이랑 같이 가도 되나요?", "유아용")
    );

    @AfterEach
    void tearDown() {
        if (storeId != null) {
            guideDocumentRepository.deleteAllByStoreId(storeId);
        }
    }

    @Test
    @DisplayName("HyDE 적용 전/후 검색 품질(hit rate·거리값)을 비교한다")
    void compareBaselineVsHyde() {
        storeId = UUID.randomUUID();
        seedDemoDocuments();

        System.out.println();
        System.out.println("========================================================================");
        System.out.printf("%-22s | %-6s | %-9s | %-6s | %-9s%n",
            "질문", "기존Hit", "기존거리", "HyDE Hit", "HyDE거리");
        System.out.println("------------------------------------------------------------------------");

        int baselineHits = 0;
        int hydeHits = 0;

        for (Case c : cases) {
            String rawEmbedding = embeddingService.embedToString(c.question());
            List<GuideDocumentScoredProjection> baselineResults =
                guideDocumentRepository.findTopKBySimilarityWithScore(storeId, rawEmbedding, 3);

            boolean baselineHit = containsKeyword(baselineResults, c.expectedKeyword());
            double baselineTopDistance = baselineResults.isEmpty() ? -1 : baselineResults.get(0).getDistance();

            String hydeAnswer = hydeQueryExpander.expand(c.question());
            String hydeEmbedding = embeddingService.embedToString(hydeAnswer);
            List<GuideDocumentScoredProjection> hydeResults =
                guideDocumentRepository.findTopKBySimilarityWithScore(storeId, hydeEmbedding, 3);

            boolean hydeHit = containsKeyword(hydeResults, c.expectedKeyword());
            double hydeTopDistance = hydeResults.isEmpty() ? -1 : hydeResults.get(0).getDistance();

            if (baselineHit) baselineHits++;
            if (hydeHit) hydeHits++;

            System.out.printf("%-22s | %-6s | %-9.4f | %-8s | %-9.4f%n",
                c.question(), baselineHit ? "O" : "X", baselineTopDistance,
                hydeHit ? "O" : "X", hydeTopDistance);
        }

        System.out.println("------------------------------------------------------------------------");
        System.out.printf("Hit rate — 기존: %d/%d (%.0f%%)  |  HyDE: %d/%d (%.0f%%)%n",
            baselineHits, cases.size(), 100.0 * baselineHits / cases.size(),
            hydeHits, cases.size(), 100.0 * hydeHits / cases.size());
        System.out.println("========================================================================");
    }

    private boolean containsKeyword(List<GuideDocumentScoredProjection> results, String keyword) {
        return results.stream()
            .map(GuideDocumentScoredProjection::getContent)
            .anyMatch(content -> content.contains(keyword));
    }

    private void seedDemoDocuments() {
        List<String> documents = List.of(
            "매장 내 주차 공간은 없으며, 건물 뒤편 공영주차장(2시간 무료)을 이용하실 수 있습니다.",
            "화장실은 매장 입구 오른쪽에 있으며, 유아용 의자가 비치되어 있습니다.",
            "영업시간은 매일 오전 8시부터 오후 9시까지이며, 브레이크타임 없이 운영합니다.",
            "매주 화요일 텀블러 지참 시 아메리카노 500원 할인 이벤트를 진행합니다.",
            "매장 와이파이 비밀번호는 카운터에 문의 시 안내해 드립니다."
        );

        for (String content : documents) {
            String embedding = embeddingService.embedToString(content);
            guideDocumentRepository.insertWithVector(storeId, content, embedding);
        }
    }
}