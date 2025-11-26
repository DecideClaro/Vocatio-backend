package com.acme.vocatio.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "deepseek")
@Getter
@Setter
public class DeepSeekProperties {

    private String apiKey;
    private String apiUrl = "https://api.deepseek.com/chat/completions";
    private String model = "deepseek-chat";
    private Duration timeout = Duration.ofSeconds(15);
    private int maxRetries = 1;

    public int getMaxAttempts() {
        return Math.max(1, maxRetries + 1);
    }
}
