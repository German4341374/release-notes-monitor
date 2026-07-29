package dev.portfolio.releasemonitor.product;

public record DashboardResponse(
        long totalProducts,
        long upToDate,
        long updateAvailable,
        long checkFailed,
        long unknown) {}
