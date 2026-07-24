package com.smartsub.store.domain;

import com.smartsub.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "common_info", length = 1000)
    private String commonInfo;

    @Column(name = "prompt_template", nullable = false, length = 2000)
    private String promptTemplate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StoreStatus status;

    private Store(
        UUID userId,
        String name,
        String commonInfo,
        String promptTemplate,
        StoreStatus status
    ) {
        this.userId = userId;
        this.name = name;
        this.commonInfo = commonInfo;
        this.promptTemplate = promptTemplate;
        this.status = status;
    }

    public static Store create(
        UUID userId,
        String name,
        String commonInfo,
        String promptTemplate,
        StoreStatus status
    ) {
        return new Store(userId, name, commonInfo, promptTemplate, status);
    }

    public void update(
        String name,
        String commonInfo,
        String promptTemplate,
        StoreStatus status
    ) {
        this.name = name;
        this.commonInfo = commonInfo;
        this.promptTemplate = promptTemplate;
        this.status = status;
    }
}
