# SmartSub V2

> 중소규모 자영업자를 위한 멀티테넌시 RAG 기반 AI QR 가이드 B2B SaaS 플랫폼

<br>

## 프로젝트 개요

매장에 배치된 QR 코드를 손님이 스캔하면, 사장님이 등록한 매장 맞춤형 데이터를 학습한 AI 챗봇이 생성되어 손님의 질문에 다국어로 실시간 응대합니다.

- **반복 응대 리소스 절감** — 주차, 와이파이, 화장실 등 하루 수십 번 발생하는 단순 질문을 AI가 처리
- **다국어 소통** — 외국어 응대 인력 없이 한/영/일/중 자동 응답
- **오프라인 고객 행동 데이터 수집** — 손님의 질문 패턴을 분석하여 사장님에게 매출 인사이트 제공 (개발 예정)

<br>

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.5 |
| ORM | Spring Data JPA, Hibernate |
| Security | Spring Security, JWT (jjwt 0.12.6) |
| AI | Spring AI 1.0.0-M6, OpenAI (text-embedding-3-small, gpt-4o-mini) |
| Database | PostgreSQL 16, pgvector |
| Build | Gradle |
| Infra | Docker |

<br>

## 핵심 아키텍처

### ① 공유 테이블 기반 멀티테넌시

B2B SaaS 특성상 수백 개 매장이 단일 DB를 공유합니다. 매장별로 독립 DB를 구성하면 인프라 비용과 커넥션 풀 관리가 비효율적이므로, 공유 테이블 + 인프라 레이어 자동 격리 방식을 채택했습니다.

HTTP 요청  
→ TenantInterceptor: JWT에서 storeId 추출  
→ ThreadLocal: 요청 스코프에 storeId 저장  
→ TenantFilterAspect: Hibernate Filter 자동 활성화  
→ SELECT * FROM p_products WHERE store_id = 'aaa' (자동 조건 추가)

### ② 테넌트 격리형 RAG

사장님이 등록한 가이드라인 텍스트를 임베딩하여 pgvector에 저장할 때 storeId를 메타데이터로 강제 바인딩합니다. 손님 질문 시 해당 매장 경계 안에서만 코사인 유사도 검색이 동작합니다.

손님 질문  
→ 질문 임베딩 (text-embedding-3-small)  
→ 코사인 유사도 검색 (WHERE store_id = :storeId)  
→ Top-K 유사 문서 추출  
→ LLM 응답 생성 (gpt-4o-mini)  
→ 다국어 응답 반환

### ③ JWT 기반 인증/인가

로그인  
→ userId로 매장 조회 → storeId 획득  
→ JWT 발급 (userId, email, role, storeId 포함)  
→ 이후 요청에서 토큰만으로 테넌트 자동 식별

<br>

## 패키지 구조

```
com.smartsub
├── global
│   ├── common          — BaseEntity (Soft Delete, JPA Auditing)
│   ├── config          — Security, JPA, ChatClient 설정
│   ├── exception       — 글로벌 예외 처리 (BusinessException, ErrorCode)
│   ├── jwt             — JwtTokenProvider, JwtAuthenticationFilter
│   └── tenant          — TenantContext, TenantInterceptor, TenantFilterAspect
├── user
│   ├── domain          — User, UserRole, UserStatus
│   ├── application     — AuthService
│   ├── infrastructure  — UserJpaRepository
│   └── presentation    — AuthController
├── store
│   ├── domain          — Store, StoreStatus
│   ├── application     — StoreService
│   ├── infrastructure  — StoreJpaRepository
│   └── presentation    — StoreController
├── product
│   ├── domain          — Product, ProductStatus, ProductUnit
│   ├── application     — ProductService
│   ├── infrastructure  — ProductJpaRepository
│   └── presentation    — ProductController
└── guide
├── domain              — GuideDocument, GuideDocumentProjection
├── application         — EmbeddingService, ChatService
├── infrastructure      — GuideDocumentJpaRepository
└── presentation        — GuideController
```
<br>

## API 명세

### 인증
| Method | URI | 설명 | 인증 |
|---|---|---|---|
| POST | /api/v1/auth/signup | 회원가입 | 불필요 |
| POST | /api/v1/auth/signin | 로그인 (JWT 발급) | 불필요 |

### 매장
| Method | URI | 설명 | 인증 |
|---|---|---|---|
| POST | /api/v1/stores | 매장 등록 | JWT (구현 예정) |
| GET | /api/v1/stores | 매장 목록 조회 | JWT (구현 예정) |
| GET | /api/v1/stores/{storeId} | 매장 단건 조회 | JWT (구현 예정) |
| PATCH | /api/v1/stores/{storeId} | 매장 수정 | JWT (구현 예정) |
| DELETE | /api/v1/stores/{storeId} | 매장 삭제 | JWT (구현 예정) |

### 상품
| Method | URI | 설명 | 인증 |
|---|---|---|---|
| POST | /api/v1/products | 상품 등록 | JWT |
| GET | /api/v1/products | 상품 목록 조회 | JWT |
| GET | /api/v1/products/{productId} | 상품 단건 조회 | JWT |
| PATCH | /api/v1/products/{productId} | 상품 수정 | JWT |
| DELETE | /api/v1/products/{productId} | 상품 삭제 | JWT |

### AI 가이드
| Method | URI | 설명 | 인증 |
|---|---|---|---|
| POST | /api/v1/guide/embed | 가이드라인 텍스트 임베딩 등록 | JWT |
| POST | /api/v1/guide/chat | 손님 QR 챗봇 질문 | 불필요 |

<br>

## 트러블슈팅

### Hibernate `float[]` → `bytea` 직렬화 문제
**문제** pgvector `vector` 타입 컬럼에 저장 시 Hibernate가 `float[]`을 `bytea`로 직렬화하여 타입 불일치 발생

**해결** JPA 엔티티 매핑 대신 네이티브 INSERT 쿼리 + `CAST(:embedding AS vector)` 적용, 조회 시 인터페이스 프로젝션으로 역직렬화 문제 우회

### TenantInterceptor 에러 응답 누락
**문제** 인증 토큰 누락 시 응답 바디가 비어있음

**해결** `@RestControllerAdvice`는 컨트롤러 이후에만 동작하므로, 인터셉터에서 직접 JSON 응답을 작성

### AuditorAware NOT NULL 제약 위반
**문제** 인증 없는 회원가입 요청에서 `created_by` 컬럼 `null` 저장 시 제약 위반

**해결** `BaseEntity`의 `createdBy`, `updatedBy` 컬럼 `nullable = false` 제거

### Spring AI 의존성 해결 실패
**문제** Spring Boot `3.5.x`에서 Spring AI 아티팩트를 찾지 못함

**해결** Spring Boot `3.4.5`로 다운그레이드, Spring Milestone 저장소 추가, BOM 버전 `1.0.0-M6` 적용

<br>

## 로컬 실행 방법

### 1. PostgreSQL + pgvector 실행
```bash
docker run -d --name smartsub-postgres -e POSTGRES_DB=smartsub -e POSTGRES_USER=smartsub -e POSTGRES_PASSWORD=smartsub1234 -p 5432:5432 pgvector/pgvector:pg16
```

### 2. pgvector 확장 활성화
```bash
docker exec -it smartsub-postgres psql -U smartsub -d smartsub -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

<br>

## 향후 구현 예정

- [ ] Kafka 기반 비동기 채팅 로그 파이프라인
- [ ] Spring Batch 기반 매출 인사이트 대시보드
- [ ] Redis 블랙리스트 (로그아웃 토큰 무효화)
- [ ] 리프레시 토큰
- [ ] CI/CD (GitHub Actions)
- [ ] 테스트 코드