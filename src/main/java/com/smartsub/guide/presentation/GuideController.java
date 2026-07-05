package com.smartsub.guide.presentation;

import com.smartsub.guide.application.ChatService;
import com.smartsub.guide.application.EmbeddingService;
import com.smartsub.guide.application.dto.ChatResult;
import com.smartsub.guide.domain.GuideDocumentProjection;
import com.smartsub.guide.presentation.request.ChatRequest;
import com.smartsub.guide.presentation.request.GuideEmbedRequest;
import com.smartsub.guide.presentation.response.ChatResponse;
import com.smartsub.guide.presentation.response.GuideDocumentResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guide")
public class GuideController {

    private final EmbeddingService embeddingService;
    private final ChatService chatService;

    // 사장님용 — 가이드라인 텍스트 임베딩 등록 (전체 교체)
    @PostMapping("/embed")
    public ResponseEntity<Void> embed(
        @Valid @RequestBody GuideEmbedRequest request
    ) {
        embeddingService.embedAndSave(request.storeId(), request.texts());
        return ResponseEntity.ok().build();
    }

    // 사장님용 — 현재 등록된 가이드 문서 목록 조회
    @GetMapping("/documents")
    public ResponseEntity<List<GuideDocumentResponse>> getDocuments(
        @RequestParam UUID storeId
    ) {
        List<GuideDocumentProjection> documents = embeddingService.getDocuments(storeId);
        List<GuideDocumentResponse> responses = documents.stream()
            .map(GuideDocumentResponse::from)
            .toList();

        return ResponseEntity.ok(responses);
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
