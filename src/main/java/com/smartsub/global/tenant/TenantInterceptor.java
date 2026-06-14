package com.smartsub.global.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String tenantId = request.getHeader("X-Tenant-Id");

        if (tenantId == null || tenantId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        TenantContext.setTenantId(UUID.fromString(tenantId));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        TenantContext.clear();
    }
}
