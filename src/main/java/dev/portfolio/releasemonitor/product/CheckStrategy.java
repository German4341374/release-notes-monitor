package dev.portfolio.releasemonitor.product;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CheckStrategy {
    STATIC_JSON("Static JSON endpoint"),
    GITHUB_RELEASES("GitHub Releases API"),
    MANUAL("Manual update");

    private final String displayName;

    CheckStrategy(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
