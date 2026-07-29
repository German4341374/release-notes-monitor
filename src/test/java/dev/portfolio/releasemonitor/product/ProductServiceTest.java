package dev.portfolio.releasemonitor.product;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.portfolio.releasemonitor.error.InvalidProductException;
import dev.portfolio.releasemonitor.version.SemanticVersionComparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductServiceTest {

    private ProductRepository repository;
    private ProductService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProductRepository.class);
        service = new ProductService(
                repository,
                mock(ProductCheckHistoryRepository.class),
                new SemanticVersionComparator());
    }

    @Test
    void automaticStrategyRequiresSourceUrl() {
        ProductRequest request = new ProductRequest(
                "Agent", "Example", "1.0.0", null, null, CheckStrategy.STATIC_JSON, null);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidProductException.class)
                .hasMessageContaining("source URL");
    }

    @Test
    void githubStrategyRejectsNonGithubHost() {
        ProductRequest request = new ProductRequest(
                "Agent",
                "Example",
                "1.0.0",
                null,
                "https://example.test/releases",
                CheckStrategy.GITHUB_RELEASES,
                null);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidProductException.class)
                .hasMessageContaining("github.com");
    }

    @Test
    void duplicateVendorAndNameIsRejected() {
        when(repository.existsByVendorIgnoreCaseAndNameIgnoreCase("Example", "Agent"))
                .thenReturn(true);
        ProductRequest request =
                new ProductRequest("Agent", "Example", "1.0.0", null, null, CheckStrategy.MANUAL, null);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidProductException.class)
                .hasMessageContaining("already exists");
    }
}
