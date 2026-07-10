package com.smartsub.guide.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * HyDE(Hypothetical Document Embeddings) 쿼리 확장기.
 *
 * 손님 질문을 그대로 임베딩하는 대신, "매장 안내 문서에 있을 법한 가상의 답변"을
 * LLM으로 먼저 생성해 그것을 검색에 사용한다. 질문("화장실 어디예요?")과 실제 문서
 * ("화장실은 입구 오른쪽에...")는 표현 형태(의문문 vs 평서문)가 달라 질문 자체를
 * 임베딩하면 유사도가 낮게 나올 수 있는데, 질문을 문서와 같은 "답변 형태"로 먼저
 * 변환한 뒤 검색하면 이 표현 격차(vocabulary gap)를 줄여 검색 품질이 올라간다.
 *
 * 이 가상 답변은 검색에만 쓰이고 손님에게 노출되지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HydeQueryExpander {

    private final ChatClient chatClient;

    public String expand(String question) {
        String hydePrompt = """
            당신은 매장 안내 문서를 작성하는 담당자입니다.
            아래 손님 질문에 대해, 매장 안내 문서에 있을 법한 답변을 1~2문장으로 작성하세요.
            실제 정보를 몰라도 괜찮습니다 — 이 답변은 손님에게 보여지지 않으며, 검색 품질 향상에만 사용됩니다.

            [손님 질문]
            %s
            """.formatted(question);

        try {
            return chatClient.prompt()
                .user(hydePrompt)
                .call()
                .content();
        } catch (Exception e) {
            log.warn("HyDE 가상 답변 생성 실패, 원본 질문으로 폴백합니다. question={}", question, e);
            return question;
        }
    }
}