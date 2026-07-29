package dev.portfolio.releasemonitor.version;

import dev.portfolio.releasemonitor.product.CheckStrategy;
import dev.portfolio.releasemonitor.product.Product;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class GitHubReleaseVersionSource implements VersionSource {

    private final ResilientHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String githubToken;

    public GitHubReleaseVersionSource(
            ResilientHttpClient httpClient,
            ObjectMapper objectMapper,
            @Value("${release-monitor.github-token:}") String githubToken) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.githubToken = githubToken == null ? "" : githubToken.trim();
    }

    @Override
    public CheckStrategy strategy() {
        return CheckStrategy.GITHUB_RELEASES;
    }

    @Override
    public VersionFetchResult fetchLatestVersion(Product product) {
        URI apiUri = toLatestReleaseApiUri(product.getVersionSourceUrl());
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Accept", "application/vnd.github+json");
        headers.put("X-GitHub-Api-Version", "2022-11-28");
        headers.put("User-Agent", "release-notes-monitor");
        if (!githubToken.isBlank()) {
            headers.put("Authorization", "Bearer " + githubToken);
        }

        HttpFetchResponse response = httpClient.get(apiUri, headers);
        if (isRateLimited(response)) {
            String reset = response.headers()
                    .firstValue("x-ratelimit-reset")
                    .flatMap(this::formatResetTime)
                    .orElse("the rate-limit reset time");
            throw new VersionSourceException("GitHub API rate limit exceeded; retry after " + reset + ".", true);
        }
        if (response.statusCode() == 404) {
            throw new VersionSourceException("No public GitHub release was found for this repository.", false);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            boolean temporary = response.statusCode() >= 500 || response.statusCode() == 429;
            throw new VersionSourceException(
                    "GitHub Releases API returned HTTP " + response.statusCode() + ".", temporary);
        }

        try {
            JsonNode tagName = objectMapper.readTree(response.body()).get("tag_name");
            if (tagName == null || !tagName.isString() || tagName.stringValue().isBlank()) {
                throw new VersionSourceException("GitHub release response has no tag_name.", false);
            }
            return new VersionFetchResult(
                    tagName.stringValue().trim(), "Version read from GitHub Releases.");
        } catch (VersionSourceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new VersionSourceException("GitHub release response could not be parsed.", false, exception);
        }
    }

    URI toLatestReleaseApiUri(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isBlank()) {
            throw new VersionSourceException("GitHub repository URL is required.", false);
        }

        URI source = URI.create(sourceUrl.trim());
        String host = source.getHost() == null ? "" : source.getHost().toLowerCase(Locale.ROOT);
        String[] segments = source.getPath().replaceAll("^/|/$", "").split("/");
        if ("github.com".equals(host) && segments.length >= 2) {
            return URI.create("https://api.github.com/repos/%s/%s/releases/latest"
                    .formatted(segments[0], removeGitSuffix(segments[1])));
        }
        if ("api.github.com".equals(host)
                && segments.length >= 3
                && "repos".equals(segments[0])) {
            return URI.create("https://api.github.com/repos/%s/%s/releases/latest"
                    .formatted(segments[1], removeGitSuffix(segments[2])));
        }
        throw new VersionSourceException(
                "GitHub source must be a github.com repository or api.github.com repository URL.", false);
    }

    private boolean isRateLimited(HttpFetchResponse response) {
        return (response.statusCode() == 403 || response.statusCode() == 429)
                && response.headers().firstValue("x-ratelimit-remaining").orElse("0").equals("0");
    }

    private java.util.Optional<String> formatResetTime(String epochSeconds) {
        try {
            return java.util.Optional.of(
                    DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(Long.parseLong(epochSeconds))));
        } catch (NumberFormatException exception) {
            return java.util.Optional.empty();
        }
    }

    private String removeGitSuffix(String repository) {
        return repository.endsWith(".git")
                ? repository.substring(0, repository.length() - 4)
                : repository;
    }
}
