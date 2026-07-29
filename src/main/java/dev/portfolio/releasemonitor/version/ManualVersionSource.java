package dev.portfolio.releasemonitor.version;

import dev.portfolio.releasemonitor.product.CheckStrategy;
import dev.portfolio.releasemonitor.product.Product;
import org.springframework.stereotype.Component;

@Component
public class ManualVersionSource implements VersionSource {

    @Override
    public CheckStrategy strategy() {
        return CheckStrategy.MANUAL;
    }

    @Override
    public VersionFetchResult fetchLatestVersion(Product product) {
        return new VersionFetchResult(product.getLatestVersion(), "Manual version value evaluated.");
    }
}
