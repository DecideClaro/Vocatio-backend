package com.acme.vocatio.config;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicEndpointsHealthIndicator implements HealthIndicator {

    private final PublicEndpointRegistry publicEndpointRegistry;

    @Override
    public Health health() {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("contextPath", publicEndpointRegistry.getContextPath());
        detail.put("basePatterns", publicEndpointRegistry.getBasePatterns());
        detail.put("readOnlyGetPatterns", publicEndpointRegistry.getReadOnlyGetPatterns());
        detail.put("exposedEndpoints", publicEndpointRegistry.getExposedEndpoints());

        return Health.up().withDetail("publicEndpoints", detail).build();
    }
}
