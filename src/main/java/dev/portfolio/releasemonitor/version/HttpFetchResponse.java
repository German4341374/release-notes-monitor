package dev.portfolio.releasemonitor.version;

import java.net.http.HttpHeaders;

public record HttpFetchResponse(int statusCode, HttpHeaders headers, byte[] body) {

    public String bodyAsUtf8() {
        return new String(body, java.nio.charset.StandardCharsets.UTF_8);
    }
}
