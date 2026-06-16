package com.smartsub.guide.application;

import com.smartsub.guide.application.dto.ChatCommand;
import com.smartsub.guide.application.dto.ChatResult;
import com.smartsub.guide.domain.GuideDocumentRepository;
import com.smartsub.guide.domain.GuideDocumentProjection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final EmbeddingService embeddingService;
    private final GuideDocumentRepository guideDocumentRepository;

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

        return new ChatResult(answer);
    }
}