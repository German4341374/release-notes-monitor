package dev.portfolio.releasemonitor.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.portfolio.releasemonitor.version.SemanticVersionComparator;
import dev.portfolio.releasemonitor.version.VersionFetchResult;
import dev.portfolio.releasemonitor.version.VersionSource;
import dev.portfolio.releasemonitor.version.VersionSourceException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductCheckServiceTest {

    private ProductService productService;
    private CheckResultRecorder recorder;
    private VersionSource source;
    private ProductCheckService checkService;
    private Product product;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        recorder = mock(CheckResultRecorder.class);
        source = mock(VersionSource.class);
        when(source.strategy()).thenReturn(CheckStrategy.STATIC_JSON);
        checkService = new ProductCheckService(
                productService,
                recorder,
                new SemanticVersionComparator(),
                List.of(source));

        product = new Product();
        product.setName("Agent");
        product.setVendor("Example");
        product.setInstalledVersion("1.2.0");
        product.setCheckStrategy(CheckStrategy.STATIC_JSON);
        when(productService.findEntity(42L)).thenReturn(product);
    }

    @Test
    void recordsAvailableUpdate() {
        when(source.fetchLatestVersion(product))
                .thenReturn(new VersionFetchResult("1.3.0", "Fetched"));
        ProductResponse expected = response(CheckStatus.UPDATE_AVAILABLE);
        when(recorder.record(eq(42L), eq("1.3.0"), eq(CheckStatus.UPDATE_AVAILABLE), anyString()))
                .thenReturn(expected);

        assertThat(checkService.check(42L)).isSameAs(expected);
    }

    @Test
    void recordsUpToDateResult() {
        when(source.fetchLatestVersion(product))
                .thenReturn(new VersionFetchResult("1.2.0", "Fetched"));
        when(recorder.record(eq(42L), eq("1.2.0"), eq(CheckStatus.UP_TO_DATE), anyString()))
                .thenReturn(response(CheckStatus.UP_TO_DATE));

        checkService.check(42L);

        verify(recorder).record(eq(42L), eq("1.2.0"), eq(CheckStatus.UP_TO_DATE), anyString());
    }

    @Test
    void recordsTemporarySourceFailureWithoutThrowing() {
        when(source.fetchLatestVersion(product))
                .thenThrow(new VersionSourceException("upstream unavailable", true));
        when(recorder.record(eq(42L), eq(null), eq(CheckStatus.CHECK_FAILED), anyString()))
                .thenReturn(response(CheckStatus.CHECK_FAILED));

        ProductResponse result = checkService.check(42L);

        assertThat(result.lastCheckStatus()).isEqualTo(CheckStatus.CHECK_FAILED);
        verify(recorder)
                .record(
                        eq(42L),
                        eq(null),
                        eq(CheckStatus.CHECK_FAILED),
                        eq("Temporary check failure: upstream unavailable"));
    }

    @Test
    void rejectsNonSemanticVersionFromSource() {
        when(source.fetchLatestVersion(product))
                .thenReturn(new VersionFetchResult("latest", "Fetched"));
        when(recorder.record(eq(42L), eq(null), eq(CheckStatus.CHECK_FAILED), anyString()))
                .thenReturn(response(CheckStatus.CHECK_FAILED));

        checkService.check(42L);

        verify(recorder)
                .record(
                        eq(42L),
                        eq(null),
                        eq(CheckStatus.CHECK_FAILED),
                        eq("Check failure: Version source returned a value that is not a semantic version."));
    }

    private ProductResponse response(CheckStatus status) {
        return new ProductResponse(
                42L,
                "Agent",
                "Example",
                "1.2.0",
                "1.3.0",
                "https://example.test/version.json",
                CheckStrategy.STATIC_JSON,
                null,
                status,
                null,
                null,
                null);
    }
}
