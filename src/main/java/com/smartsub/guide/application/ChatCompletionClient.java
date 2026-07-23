package com.smartsub.guide.application;

import com.smartsub.global.exception.BusinessException;
import com.smartsub.global.exception.ErrorCode;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

/**
 * 손님 응대용 최종 LLM 응답 생성을 담당하는 게이트웨이.
 *
 * ChatService.chat() 내부에서 직접 호출하면(같은 빈 안에서의 self-invocation)
 * Spring AOP 프록시를 우회해 @RateLimiter가 적용되지 않으므로, 별도 빈으로 분리해
 * 다른 빈(ChatService)에서 프록시를 거쳐 호출되도록 한다.
 *
 * HyDE와 달리 이 호출은 폴백이 불가능하다(손님에게 보여줄 실제 답변을 생성하는
 * 단계라 대체 답변이 없음) — 그래서 실패 시 원본 질문 대신 429로 명확히 응답한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatCompletionClient {

    private final ChatClient chatClient;

    @RateLimiter(name = "openai", fallbackMethod = "fallback")
    public ChatResponse complete(String prompt) {
        return chatClient.prompt()
            .user(prompt)
            .call()
            .chatResponse();
    }

    @SuppressWarnings("unused")
    private ChatResponse fallback(String prompt, RequestNotPermitted ex) {
        log.warn("OpenAI 호출 한도 초과로 응답 생성 요청이 거절되었습니다.");
        throw new BusinessException(ErrorCode.CHAT_SERVICE_BUSY);
    }
}