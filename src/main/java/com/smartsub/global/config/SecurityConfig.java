package com.smartsub.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 테스트 편의를 위해 CSRF 보안 비활성화
            // TODO: 상용 배포 전 CSRF 방어 전략 수립 필요 (Stateless한 JWT 사용 시 disable 유지 가능, 단 Cookie 사용 시 활성화 및 토큰 검증 필요)
            .csrf(AbstractHttpConfigurer::disable)

            // TODO: JWT 또는 OAuth2 기반의 인증 필터(UsernamePasswordAuthenticationFilter 이전 단계) 추가 필요
            // TODO: CORS(Cross-Origin Resource Sharing) 설정 추가 필요 (프론트엔드 React 서버 주소만 허용하도록)
            // TODO: 세션 정책을 STATELESS로 설정 필요 (.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)))

            .authorizeHttpRequests(auth -> auth
                // /api/v1/products로 시작하는 모든 요청은 인증 없이 허용
                // TODO: 손님용 조회 API(GET)만 permitAll()로 열고, 사장님용 등록/수정/삭제(POST/PATCH/DELETE)는 인가 권한(hasRole('OWNER')) 체크하도록 분리 필요
                // TODO: QR 접속용 AI 가이드 대화 API 엔드포인트(/api/v1/guide/**) permitAll() 추가 필요
                .requestMatchers("/api/v1/products/**").permitAll()
                .requestMatchers("/api/v1/products").permitAll()

                /* ======= [추가] 매장(Store) 테스트용 프리패스 경로 ======= */
                // TODO: 서비스 오픈 시 매장 등록/수정/삭제는 플랫폼 관리자(ADMIN) 또는 해당 매장 OWNER만 가능하도록 인가 변경 필요
                .requestMatchers("/api/v1/stores/**").permitAll()
                .requestMatchers("/api/v1/stores").permitAll()

                // 그 외의 요청은 인증 필요
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
