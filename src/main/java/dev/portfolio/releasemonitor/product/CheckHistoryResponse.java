package dev.portfolio.releasemonitor.product;

import java.time.Instant;

public record CheckHistoryResponse(
        Long id,
        Instant checkedAt,
        String installedVersion,
        String detectedVersion,
        CheckStatus status,
        String message) {

    public static CheckHistoryResponse from(ProductCheckHistory history) {
        return new CheckHistoryResponse(
                history.getId(),
                history.getCheckedAt(),
                history.getInstalledVersion(),
                history.getDetectedVersion(),
                history.getStatus(),
                history.getMessage());
    }
}
