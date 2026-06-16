package com.smartsub.guide.presentation;

import com.smartsub.guide.application.ChatService;
import com.smartsub.guide.application.EmbeddingService;
import com.smartsub.guide.application.dto.ChatResult;
import com.smartsub.guide.presentation.request.ChatRequest;
import com.smartsub.guide.presentation.request.GuideEmbedRequest;
import com.smartsub.guide.presentation.response.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guide")
public class GuideController {

    private final EmbeddingService embeddingService;
    private final ChatService chatService;

    // 사장님용 — 가이드라인 텍스트 임베딩 등록
    @PostMapping("/embed")
    public ResponseEntity<Void> embed(
        @Valid @RequestBody GuideEmbedRequest request
    ) {
        embeddingService.embedAndSave(request.storeId(), request.texts());
        return ResponseEntity.ok().build();
    }

    // 손님용 — QR 챗봇 질문
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
        @Valid @RequestBody ChatRequest request
    ) {
        ChatResult result = chatService.chat(request.toCommand());
        return ResponseEntity.ok(ChatResponse.from(result));
    }
}
