package com.smartsub.guide.domain;

/**
 * {@link GuideDocumentProjection}에 pgvector 코사인 거리(distance)를 추가한 버전.
 * 운영 코드(ChatService)는 거리값이 필요 없어 기존 프로젝션을 그대로 쓰고,
 * 이 프로젝션은 검색 품질 비교(HyDE 적용 전/후 등) 목적의 평가 코드에서만 사용한다.
 * 값이 작을수록(0에 가까울수록) 더 유사하다.
 */
public interface GuideDocumentScoredProjection {
    String getId();
    String getStoreId();
    String getContent();
    Double getDistance();
}