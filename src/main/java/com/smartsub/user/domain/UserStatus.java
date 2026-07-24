package com.smartsub.user.domain;

public enum UserStatus {
    ACTIVE,     // 활성 상태
    INACTIVE,   // 비활성 상태 (예: 이메일 인증 대기, 휴면 계정 등)
    WITHDRAWN   // 탈퇴 상태 (예: 사용자가 계정을 삭제한 경우)
}
