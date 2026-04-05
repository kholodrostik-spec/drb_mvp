package com.drb.DrbMVP.security.filters;

import com.drb.DrbMVP.repository.ApiLogRepository;
import com.drb.DrbMVP.service.CachedBodyHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class ApiLogFilter extends OncePerRequestFilter {

    private final ApiLogRepository apiLogRepository;

    public ApiLogFilter(ApiLogRepository apiLogRepository) {
        this.apiLogRepository = apiLogRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (shouldSkip(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String contentType = request.getContentType();
        boolean isMultipart = contentType != null && contentType.toLowerCase().startsWith("multipart/");

        long startTime = System.currentTimeMillis();

        if (isMultipart) {
            filterChain.doFilter(request, response);
        } else {
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
            filterChain.doFilter(cachedRequest, response);
            request = cachedRequest;
        }

        long duration = System.currentTimeMillis() - startTime;

        String method = request.getMethod();
        int status = response.getStatus();

        String userEmail = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            userEmail = auth.getName();
        }

        String queryParams = request.getQueryString();
        String requestBody;

        if (isMultipart) {
            requestBody = "[multipart/form-data - binary content skipped]";
        } else if (request instanceof CachedBodyHttpServletRequest cachedRequest) {
            requestBody = cachedRequest.getBody();
            if (requestBody != null) {
                requestBody = requestBody.replace("\u0000", "");
            }
        } else {
            requestBody = null;
        }

        try {
            apiLogRepository.save(userEmail, method, path, queryParams, requestBody, status, duration);
        } catch (Exception e) {
            log.error("Failed to save API log: {}", e.getMessage());
        }
    }

    private boolean shouldSkip(String path) {
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/auth/") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-resources");
    }
}
