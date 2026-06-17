package com.smartsub.guide.application;

import com.smartsub.guide.application.dto.ChatCommand;
import com.smartsub.guide.application.dto.ChatResult;
import com.smartsub.guide.domain.ChatLog;
import com.smartsub.guide.domain.ChatLogRepository;
import com.smartsub.guide.domain.GuideDocumentRepository;
import com.smartsub.guide.domain.GuideDocumentProjection;
import com.smartsub.guide.domain.Language;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final EmbeddingService embeddingService;
    private final GuideDocumentRepository guideDocumentRepository;
    private final ChatLogRepository chatLogRepository;

    public ChatResult chat(ChatCommand command) {
        String questionEmbedding = embeddingService.embedToString(command.question());

        List<GuideDocumentProjection> documents = guideDocumentRepository
            .findTopKBySimilarity(command.storeId(), questionEmbedding, 3);

        String context = documents.stream()
            .map(GuideDocumentProjection::getContent)
            .collect(Collectors.joining("\n\n"));

        String prompt = """
            당신은 매장의 AI 점장입니다. 아래 매장 정보를 바탕으로 손님의 질문에 답변하세요.
            손님이 사용하는 언어로 답변하세요 (한국어, 영어, 일본어, 중국어 등).
            매장 정보에 없는 내용은 모른다고 답변하세요.
                        
            [매장 정보]
            %s
                        
            [손님 질문]
            %s
            """.formatted(context, command.question());

        String answer = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        Language language = detectLanguage(command.question());
        ChatLog chatLog = ChatLog.create(
            command.storeId(),
            command.tableNumber(),
            command.question(),
            answer,
            language
        );

        long start = System.currentTimeMillis();
        chatLogRepository.save(chatLog);
        long elapsed = System.currentTimeMillis() - start;
        log.info("채팅 로그 저장 시간: {}ms", elapsed);

        return new ChatResult(answer);
    }

    private Language detectLanguage(String text) {
        if (text.matches(".*[가-힣].*")) return Language.KOREAN;
        if (text.matches(".*[\\u3040-\\u30FF].*")) return Language.JAPANESE;
        if (text.matches(".*[\\u4E00-\\u9FFF].*")) return Language.CHINESE;
        return Language.ENGLISH;
    }
}