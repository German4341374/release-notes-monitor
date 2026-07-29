package dev.portfolio.releasemonitor.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;

public class HttpUrlValidator implements ConstraintValidator<ValidHttpUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            URI uri = URI.create(value.trim());
            boolean supportedScheme = "http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme());
            return supportedScheme && uri.getHost() != null && uri.getUserInfo() == null;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
