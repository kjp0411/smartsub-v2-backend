package com.smartsub.guide.presentation.request;

import com.smartsub.guide.application.dto.ChatCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChatRequest(
    @NotNull(message = "매장 ID는 필수입니다.")
    UUID storeId,

    @NotBlank(message = "테이블 번호는 필수입니다.")
    String tableNumber,

    @NotBlank(message = "질문은 필수입니다.")
    String question
) {
    public ChatCommand toCommand() {
        return new ChatCommand(storeId, tableNumber, question);
    }
}
