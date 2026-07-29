package dev.portfolio.releasemonitor.version;

import dev.portfolio.releasemonitor.product.CheckStrategy;
import dev.portfolio.releasemonitor.product.Product;

public interface VersionSource {

    CheckStrategy strategy();

    VersionFetchResult fetchLatestVersion(Product product);
}
