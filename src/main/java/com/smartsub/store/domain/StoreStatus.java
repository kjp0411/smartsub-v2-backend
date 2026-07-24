package com.smartsub.store.domain;

public enum StoreStatus {
    ACTIVE,       // 정상 운영 및 AI 가이드 활성화
    PAUSED,       // 임시 중지 (구독 정산 지연 또는 사장님 수동 중지)
    TERMINATED    // 서비스 해지 및 종료
}
