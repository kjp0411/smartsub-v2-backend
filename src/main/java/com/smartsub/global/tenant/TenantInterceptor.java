package com.smartsub.global.tenant;

import com.smartsub.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request,
        HttpServletResponse response,
        Object handler) throws IOException {

        String bearer = request.getHeader("Authorization");

        if (bearer == null || !bearer.startsWith("Bearer ")) {
            writeErrorResponse(response, 401, "UNAUTHORIZED", "인증 토큰이 없습니다.");
            return false;
        }

        String token = bearer.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            writeErrorResponse(response, 401, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
            return false;
        }

        UUID storeId = jwtTokenProvider.getStoreId(token);

        if (storeId == null) {
            writeErrorResponse(response, 403, "STORE_NOT_ASSIGNED", "매장이 등록되지 않은 계정입니다.");
            return false;
        }

        TenantContext.setTenantId(storeId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
        HttpServletResponse response,
        Object handler,
        Exception ex) {
        TenantContext.clear();
    }

    private void writeErrorResponse(HttpServletResponse response,
        int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            String.format("{\"status\":%d,\"code\":\"%s\",\"message\":\"%s\"}", status, code, message)
        );
    }
}