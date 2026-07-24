package com.smartsub.guide.presentation.response;

import com.smartsub.guide.application.dto.ChatResult;

public record ChatResponse(
    String answer
) {
    public static ChatResponse from(ChatResult result) {
        return new ChatResponse(result.answer());
    }
}
