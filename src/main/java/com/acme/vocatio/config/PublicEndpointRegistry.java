package com.acme.vocatio.config;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@RequiredArgsConstructor
public class PublicEndpointRegistry {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> BASE_PATTERNS = List.of(
            "/",
            "/error",
            "/auth/login",
            "/auth/register",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/health"
    );

    private static final List<String> READ_ONLY_GET_PATTERNS = List.of(
            "/api/careers",
            "/api/careers/**"
    );

    private final ServerProperties serverProperties;

    public List<String> getBasePatterns() {
        return BASE_PATTERNS;
    }

    public List<String> getReadOnlyGetPatterns() {
        return READ_ONLY_GET_PATTERNS;
    }

    public List<String> getExposedEndpoints() {
        return Stream.concat(BASE_PATTERNS.stream(), READ_ONLY_GET_PATTERNS.stream())
                .map(this::withContextPath)
                .distinct()
                .toList();
    }

    public RequestMatcher[] asRequestMatchers() {
        return BASE_PATTERNS.stream()
                .map(this::contextAwareMatcher)
                .toArray(RequestMatcher[]::new);
    }

    public RequestMatcher[] asReadOnlyGetMatchers() {
        return READ_ONLY_GET_PATTERNS.stream()
                .map(pattern -> contextAwareMatcher(pattern, HttpMethod.GET))
                .toArray(RequestMatcher[]::new);
    }

    public boolean isPublicRequest(jakarta.servlet.http.HttpServletRequest request) {
        return Stream.concat(
                        Arrays.stream(asRequestMatchers()),
                        Arrays.stream(asReadOnlyGetMatchers())
                )
                .anyMatch(matcher -> matcher.matches(request));
    }

    public String getContextPath() {
        String contextPath = serverProperties != null && serverProperties.getServlet() != null
                ? Objects.toString(serverProperties.getServlet().getContextPath(), "")
                : "";

        if (contextPath == null || contextPath.isBlank()) {
            return "/";
        }

        return contextPath;
    }

    private RequestMatcher contextAwareMatcher(String pattern) {
        final String normalizedPattern = normalizePattern(pattern);
        return request -> {
            String path = normalizeRequestUri(request.getRequestURI(), request.getContextPath());
            return PATH_MATCHER.match(normalizedPattern, path);
        };
    }

    private RequestMatcher contextAwareMatcher(String pattern, HttpMethod method) {
        final String normalizedPattern = normalizePattern(pattern);
        return request -> {
            if (method != null && !method.matches(request.getMethod())) {
                return false;
            }

            String path = normalizeRequestUri(request.getRequestURI(), request.getContextPath());
            return PATH_MATCHER.match(normalizedPattern, path);
        };
    }

    private String withContextPath(String pattern) {
        String normalizedPattern = normalizePattern(pattern);
        String contextPath = serverProperties != null && serverProperties.getServlet() != null
                ? Objects.toString(serverProperties.getServlet().getContextPath(), "")
                : "";

        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath)) {
            return normalizedPattern;
        }

        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        if (contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }

        if ("/".equals(normalizedPattern)) {
            return contextPath.isEmpty() ? "/" : contextPath;
        }

        return contextPath + normalizedPattern;
    }

    private String normalizePattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return "/";
        }

        return pattern.startsWith("/") ? pattern : "/" + pattern;
    }

    private String normalizeRequestUri(String requestUri, String contextPath) {
        String normalizedUri = (requestUri == null || requestUri.isBlank()) ? "/" : requestUri;
        String ctx = (contextPath == null) ? "" : contextPath;

        if (!ctx.isEmpty() && normalizedUri.startsWith(ctx)) {
            normalizedUri = normalizedUri.substring(ctx.length());
        }

        if (normalizedUri.isEmpty()) {
            return "/";
        }

        return normalizedUri.startsWith("/") ? normalizedUri : "/" + normalizedUri;
    }
}
