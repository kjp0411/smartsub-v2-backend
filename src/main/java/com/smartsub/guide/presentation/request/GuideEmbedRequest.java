package com.smartsub.guide.presentation.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record GuideEmbedRequest(
    @NotNull(message = "매장 ID는 필수입니다.")
    UUID storeId,

    @NotEmpty(message = "가이드 텍스트는 최소 1개 이상이어야 합니다.")
    List<String> texts
) {
}
