package com.smartsub.guide.presentation.response;

import com.smartsub.guide.domain.GuideDocumentProjection;

public record GuideDocumentResponse(
    String id,
    String content
) {
    public static GuideDocumentResponse from(GuideDocumentProjection projection) {
        return new GuideDocumentResponse(projection.getId(), projection.getContent());
    }
}
