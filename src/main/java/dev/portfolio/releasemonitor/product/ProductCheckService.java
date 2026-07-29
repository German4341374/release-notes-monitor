package dev.portfolio.releasemonitor.product;

import dev.portfolio.releasemonitor.error.InvalidProductException;
import dev.portfolio.releasemonitor.version.SemanticVersionComparator;
import dev.portfolio.releasemonitor.version.VersionFetchResult;
import dev.portfolio.releasemonitor.version.VersionSource;
import dev.portfolio.releasemonitor.version.VersionSourceException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ProductCheckService {

    private final ProductService productService;
    private final CheckResultRecorder resultRecorder;
    private final SemanticVersionComparator comparator;
    private final Map<CheckStrategy, VersionSource> sources;

    public ProductCheckService(
            ProductService productService,
            CheckResultRecorder resultRecorder,
            SemanticVersionComparator comparator,
            List<VersionSource> versionSources) {
        this.productService = productService;
        this.resultRecorder = resultRecorder;
        this.comparator = comparator;
        this.sources = new EnumMap<>(CheckStrategy.class);
        versionSources.forEach(source -> this.sources.put(source.strategy(), source));
    }

    public ProductResponse check(Long productId) {
        Product product = productService.findEntity(productId);
        VersionSource source = sources.get(product.getCheckStrategy());
        if (source == null) {
            throw new InvalidProductException("No version source is configured for this strategy.");
        }

        try {
            VersionFetchResult fetched = source.fetchLatestVersion(product);
            if (fetched.version() == null || fetched.version().isBlank()) {
                return resultRecorder.record(
                        productId,
                        null,
                        CheckStatus.UNKNOWN,
                        "No latest version is available for comparison.");
            }
            if (!comparator.isValid(fetched.version())) {
                throw new VersionSourceException(
                        "Version source returned a value that is not a semantic version.", false);
            }

            boolean updateAvailable =
                    comparator.isUpdateAvailable(product.getInstalledVersion(), fetched.version());
            CheckStatus status =
                    updateAvailable ? CheckStatus.UPDATE_AVAILABLE : CheckStatus.UP_TO_DATE;
            String message = updateAvailable
                    ? "A newer version is available."
                    : "Installed version is current.";
            return resultRecorder.record(productId, fetched.version(), status, message);
        } catch (VersionSourceException exception) {
            String prefix = exception.isTemporary() ? "Temporary check failure: " : "Check failure: ";
            return resultRecorder.record(
                    productId, null, CheckStatus.CHECK_FAILED, prefix + exception.getMessage());
        }
    }
}
