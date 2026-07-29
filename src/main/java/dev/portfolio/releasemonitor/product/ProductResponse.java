package dev.portfolio.releasemonitor.product;

import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String vendor,
        String installedVersion,
        String latestVersion,
        String versionSourceUrl,
        CheckStrategy checkStrategy,
        Instant lastCheckedAt,
        CheckStatus lastCheckStatus,
        String notes,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getVendor(),
                product.getInstalledVersion(),
                product.getLatestVersion(),
                product.getVersionSourceUrl(),
                product.getCheckStrategy(),
                product.getLastCheckedAt(),
                product.getLastCheckStatus(),
                product.getNotes(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
