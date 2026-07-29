package dev.portfolio.releasemonitor.version;

import dev.portfolio.releasemonitor.product.CheckStrategy;
import dev.portfolio.releasemonitor.product.Product;
import java.net.URI;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class StaticJsonVersionSource implements VersionSource {

    private static final String[] VERSION_FIELDS = {"version", "latestVersion", "tag_name"};

    private final ResilientHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public StaticJsonVersionSource(ResilientHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public CheckStrategy strategy() {
        return CheckStrategy.STATIC_JSON;
    }

    @Override
    public VersionFetchResult fetchLatestVersion(Product product) {
        URI source = requireSource(product);
        HttpFetchResponse response =
                httpClient.get(source, Map.of("Accept", "application/json", "User-Agent", "release-notes-monitor"));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            boolean temporary = response.statusCode() >= 500 || response.statusCode() == 429;
            throw new VersionSourceException(
                    "Static JSON source returned HTTP " + response.statusCode() + ".", temporary);
        }

        try {
            JsonNode root = objectMapper.readTree(response.body());
            for (String field : VERSION_FIELDS) {
                JsonNode value = root.get(field);
                if (value != null && value.isString() && !value.stringValue().isBlank()) {
                    return new VersionFetchResult(
                            value.stringValue().trim(), "Version read from static JSON.");
                }
            }
        } catch (Exception exception) {
            throw new VersionSourceException("Static JSON response could not be parsed.", false, exception);
        }
        throw new VersionSourceException(
                "Static JSON must contain version, latestVersion, or tag_name.", false);
    }

    private URI requireSource(Product product) {
        if (product.getVersionSourceUrl() == null || product.getVersionSourceUrl().isBlank()) {
            throw new VersionSourceException("Static JSON source URL is required.", false);
        }
        return URI.create(product.getVersionSourceUrl());
    }
}
