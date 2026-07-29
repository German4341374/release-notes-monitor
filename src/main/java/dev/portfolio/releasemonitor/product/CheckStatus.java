package dev.portfolio.releasemonitor.product;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CheckStatus {
    UP_TO_DATE("Up to date"),
    UPDATE_AVAILABLE("Update available"),
    CHECK_FAILED("Check failed"),
    UNKNOWN("Unknown");

    private final String displayName;

    CheckStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
