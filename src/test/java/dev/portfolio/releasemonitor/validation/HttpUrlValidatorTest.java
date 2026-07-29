package dev.portfolio.releasemonitor.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HttpUrlValidatorTest {

    private final HttpUrlValidator validator = new HttpUrlValidator();

    @Test
    void acceptsHttpsUrl() {
        assertThat(validator.isValid("https://example.test/releases.json", null)).isTrue();
    }

    @Test
    void rejectsUnsupportedScheme() {
        assertThat(validator.isValid("file:///etc/passwd", null)).isFalse();
    }

    @Test
    void rejectsUrlWithCredentials() {
        assertThat(validator.isValid("https://user:password@example.test/data", null)).isFalse();
    }
}
