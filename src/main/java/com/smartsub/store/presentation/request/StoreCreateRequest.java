package com.smartsub.store.presentation.request;

import com.smartsub.store.application.dto.StoreCreateCommand;
import com.smartsub.store.domain.StoreStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StoreCreateRequest(
    @NotNull(message = "사장님 ID는 필수입니다.")
    UUID userId,

    @NotBlank(message = "매장 이름은 필수입니다.")
    @Size(max = 100, message = "매장 이름은 최대 100자까지 입력할 수 있습니다.")
    String name,

    @Size(max = 1000, message = "매장 공통 안내 사항은 최대 1000자까지 입력할 수 있습니다.")
    String commonInfo,

    @NotBlank(message = "AI 프롬프트 템플릿은 필수입니다.")
    @Size(max = 2000, message = "AI 프롬프트 템플릿은 최대 2000자까지 입력할 수 있습니다.")
    String promptTemplate,

    @NotNull(message = "매장 상태는 필수입니다.")
    StoreStatus status
) {
    public StoreCreateCommand toCommand() {
        return new StoreCreateCommand(
            userId,
            name,
            commonInfo,
            promptTemplate,
            status
        );
    }
}