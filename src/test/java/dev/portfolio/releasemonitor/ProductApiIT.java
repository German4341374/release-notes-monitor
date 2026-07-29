package dev.portfolio.releasemonitor;

import dev.portfolio.releasemonitor.product.CheckStatus;
import dev.portfolio.releasemonitor.product.CheckStrategy;
import dev.portfolio.releasemonitor.product.DashboardResponse;
import dev.portfolio.releasemonitor.product.ProductRequest;
import dev.portfolio.releasemonitor.product.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureRestTestClient
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "release-monitor.scheduling.enabled=false")
class ProductApiIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18.4-alpine3.24");

    @Autowired
    private RestTestClient rest;

    @Test
    void healthEndpointReportsUp() {
        rest.get()
                .uri("/health")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo("UP");
    }

    @Test
    void flywaySeedPopulatesDashboard() {
        rest.get()
                .uri("/api/dashboard")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(DashboardResponse.class)
                .value(dashboard -> {
                    org.assertj.core.api.Assertions.assertThat(dashboard.totalProducts())
                            .isGreaterThanOrEqualTo(8);
                    org.assertj.core.api.Assertions.assertThat(dashboard.updateAvailable())
                            .isGreaterThanOrEqualTo(3);
                });
    }

    @Test
    void createAndReadManualProduct() {
        ProductRequest request = new ProductRequest(
                "Demo CLI",
                "Portfolio Vendor",
                "1.0.0",
                "1.1.0",
                null,
                CheckStrategy.MANUAL,
                "Created by an integration test.");

        ProductResponse created = createProduct(request);

        rest.get()
                .uri("/api/products/{id}", created.id())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ProductResponse.class)
                .value(product -> org.assertj.core.api.Assertions.assertThat(product.name())
                        .isEqualTo("Demo CLI"));
    }

    @Test
    void manualCheckCreatesHistoryAndUpdateStatus() {
        ProductRequest request = new ProductRequest(
                "Manual Check Target",
                "Portfolio Vendor",
                "2.0.0",
                "2.1.0",
                null,
                CheckStrategy.MANUAL,
                null);
        ProductResponse created = createProduct(request);

        rest.post()
                .uri("/api/products/{id}/check", created.id())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ProductResponse.class)
                .value(product -> org.assertj.core.api.Assertions.assertThat(product.lastCheckStatus())
                        .isEqualTo(CheckStatus.UPDATE_AVAILABLE));

        rest.get()
                .uri("/api/products/{id}/history", created.id())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()")
                .isEqualTo(1);
    }

    @Test
    void invalidRequestUsesProblemDetails() {
        ProductRequest invalid = new ProductRequest(
                "", "Vendor", "not-a-version", null, null, CheckStrategy.MANUAL, null);

        rest.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalid)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectHeader()
                .contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.errors")
                .exists();
    }

    @Test
    void statusFilterReturnsOnlyMatchingProducts() {
        rest.get()
                .uri("/api/products?status=UPDATE_AVAILABLE")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ProductResponse[].class)
                .value(products -> org.assertj.core.api.Assertions.assertThat(products)
                        .isNotEmpty()
                        .allMatch(product -> product.lastCheckStatus() == CheckStatus.UPDATE_AVAILABLE));
    }

    private ProductResponse createProduct(ProductRequest request) {
        return rest.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(ProductResponse.class)
                .returnResult()
                .getResponseBody();
    }
}
