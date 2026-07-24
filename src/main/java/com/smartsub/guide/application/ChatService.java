package com.smartsub.guide.application;

import com.smartsub.guide.application.dto.ChatCommand;
import com.smartsub.guide.application.dto.ChatLogEvent;
import com.smartsub.guide.application.dto.ChatResult;
import com.smartsub.guide.domain.ChatCategory;
import com.smartsub.guide.domain.GuideDocumentRepository;
import com.smartsub.guide.domain.GuideDocumentProjection;
import com.smartsub.guide.domain.Language;
import com.smartsub.guide.infrastructure.ChatLogProducer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatCompletionClient chatCompletionClient;
    private final EmbeddingService embeddingService;
    private final GuideDocumentRepository guideDocumentRepository;
    private final ChatLogProducer chatLogProducer;
    private final HydeQueryExpander hydeQueryExpander;
    private final MeterRegistry meterRegistry;

    record LlmChatResponse(String answer, String category) {}

    public ChatResult chat(ChatCommand command) {
        long startedAt = System.currentTimeMillis();

        String hypotheticalAnswer = timeStage("hyde", () -> hydeQueryExpander.expand(command.question()));
        String questionEmbedding = timeStage("embedding", () -> embeddingService.embedToString(hypotheticalAnswer));

        List<GuideDocumentProjection> documents = timeStage("db_search", () ->
            guideDocumentRepository.findTopKBySimilarity(command.storeId(), questionEmbedding, 3));

        String context = documents.stream()
            .map(GuideDocumentProjection::getContent)
            .collect(Collectors.joining("\n\n"));

        String categoryList = Arrays.stream(ChatCategory.values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

        BeanOutputConverter<LlmChatResponse> outputConverter = new BeanOutputConverter<>(LlmChatResponse.class);

        String prompt = """
            당신은 매장의 AI 점장입니다. 아래 매장 정보를 바탕으로 손님의 질문에 답변하세요.
            손님에게 항상 정중한 존댓말로 답변하세요.
            손님이 사용하는 언어로 답변하세요 (한국어, 영어, 일본어, 중국어 등).
            매장 정보에 없는 내용은 정중하게 모른다고 답변하세요.
    
            카테고리 분류는 답변 가능 여부와 무관하게, 질문이 다루는 주제로만 판단하세요.
            예를 들어 이벤트 정보가 없어 "모른다"고 답하더라도, 질문이 이벤트에 대한 것이면 category는 EVENT입니다.
            category 값은 반드시 다음 목록 중 하나의 영문 대문자여야 합니다 (번역하지 마세요): %s
            질문 자체의 주제가 목록 중 무엇에도 해당하지 않을 때만 ETC로 분류하세요.

            [매장 정보]
            %s

            [손님 질문]
            %s

            %s
            """.formatted(categoryList, context, command.question(), outputConverter.getFormat());

        ChatResponse chatResponse = timeStage("final_completion", () ->
            chatCompletionClient.complete(prompt));

        String rawContent = chatResponse.getResult().getOutput().getText();
        LlmChatResponse llmResponse = outputConverter.convert(rawContent);

        String answer = llmResponse.answer();
        ChatCategory category = ChatCategory.from(llmResponse.category());

        Usage usage = chatResponse.getMetadata().getUsage();
        Integer promptTokens = usage != null ? usage.getPromptTokens() : null;
        Integer completionTokens = usage != null ? usage.getCompletionTokens() : null;

        long latencyMs = System.currentTimeMillis() - startedAt;

        Language language = detectLanguage(command.question());
        ChatLogEvent event = new ChatLogEvent(
            command.storeId(),
            command.tableNumber(),
            command.question(),
            answer,
            language,
            category.name(),
            latencyMs,
            promptTokens,
            completionTokens
        );
        chatLogProducer.send(event);

        return new ChatResult(answer, category.name());
    }

    private Language detectLanguage(String text) {
        if (text.matches(".*[가-힣].*")) return Language.KOREAN;
        if (text.matches(".*[\\u3040-\\u30FF].*")) return Language.JAPANESE;
        if (text.matches(".*[\\u4E00-\\u9FFF].*")) return Language.CHINESE;
        return Language.ENGLISH;
    }

    private <T> T timeStage(String stage, java.util.function.Supplier<T> block) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return block.get();
        } finally {
            sample.stop(Timer.builder("chat.stage.duration")
                .description("SmartSub chat pipeline stage duration")
                .tag("stage", stage)
                .publishPercentileHistogram()
                .register(meterRegistry));
        }
    }
}