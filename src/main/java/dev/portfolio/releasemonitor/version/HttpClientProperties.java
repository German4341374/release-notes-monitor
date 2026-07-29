package dev.portfolio.releasemonitor.version;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("release-monitor.http")
public record HttpClientProperties(
        @NotNull Duration connectTimeout,
        @NotNull Duration requestTimeout,
        @Min(0) @Max(4) int maxRetries,
        @NotNull Duration retryBaseDelay,
        @Min(1024) @Max(5_242_880) int maxResponseBytes) {}
