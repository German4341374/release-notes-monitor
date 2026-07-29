package dev.portfolio.releasemonitor.version;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ResilientHttpClient {

    private static final Set<Integer> RETRYABLE_STATUS_CODES = Set.of(429, 500, 502, 503, 504);

    private final HttpClient client;
    private final HttpClientProperties properties;

    public ResilientHttpClient(HttpClientProperties properties) {
        this.properties = properties;
        this.client = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public HttpFetchResponse get(URI uri, Map<String, String> headers) {
        VersionSourceException lastFailure = null;
        for (int attempt = 0; attempt <= properties.maxRetries(); attempt++) {
            try {
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
                        .timeout(properties.requestTimeout())
                        .GET();
                headers.forEach(requestBuilder::header);

                HttpResponse<byte[]> response =
                        client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
                if (response.body().length > properties.maxResponseBytes()) {
                    throw new VersionSourceException("Version source response is too large.", false);
                }

                if (!RETRYABLE_STATUS_CODES.contains(response.statusCode())
                        || attempt == properties.maxRetries()) {
                    return new HttpFetchResponse(response.statusCode(), response.headers(), response.body());
                }
                lastFailure =
                        new VersionSourceException("Version source returned a temporary HTTP error.", true);
            } catch (IOException exception) {
                lastFailure =
                        new VersionSourceException("Version source could not be reached.", true, exception);
                if (attempt == properties.maxRetries()) {
                    throw lastFailure;
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new VersionSourceException("Version check was interrupted.", true, exception);
            }

            pauseBeforeRetry(attempt);
        }
        throw lastFailure == null
                ? new VersionSourceException("Version check failed.", true)
                : lastFailure;
    }

    private void pauseBeforeRetry(int attempt) {
        long multiplier = 1L << attempt;
        Duration delay = properties.retryBaseDelay().multipliedBy(multiplier);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new VersionSourceException("Version check was interrupted.", true, exception);
        }
    }
}
