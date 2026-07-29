package dev.portfolio.releasemonitor.product;

import dev.portfolio.releasemonitor.error.InvalidProductException;
import dev.portfolio.releasemonitor.error.ResourceNotFoundException;
import dev.portfolio.releasemonitor.version.SemanticVersionComparator;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCheckHistoryRepository historyRepository;
    private final SemanticVersionComparator versionComparator;

    public ProductService(
            ProductRepository productRepository,
            ProductCheckHistoryRepository historyRepository,
            SemanticVersionComparator versionComparator) {
        this.productRepository = productRepository;
        this.historyRepository = historyRepository;
        this.versionComparator = versionComparator;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(CheckStatus status) {
        List<Product> products = status == null
                ? productRepository.findAllByOrderByVendorAscNameAsc()
                : productRepository.findByLastCheckStatusOrderByVendorAscNameAsc(status);
        return products.stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(requireProduct(id));
    }

    @Transactional(readOnly = true)
    public Product findEntity(Long id) {
        return requireProduct(id);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        validateRequest(request, null);
        Product product = new Product();
        apply(product, request);
        try {
            return ProductResponse.from(productRepository.save(product));
        } catch (DataIntegrityViolationException exception) {
            throw new InvalidProductException("A product with this vendor and name already exists.");
        }
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = requireProduct(id);
        validateRequest(request, id);
        apply(product, request);
        try {
            return ProductResponse.from(productRepository.save(product));
        } catch (DataIntegrityViolationException exception) {
            throw new InvalidProductException("A product with this vendor and name already exists.");
        }
    }

    @Transactional
    public void delete(Long id) {
        Product product = requireProduct(id);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public List<CheckHistoryResponse> history(Long id) {
        requireProduct(id);
        return historyRepository.findTop50ByProductIdOrderByCheckedAtDesc(id).stream()
                .map(CheckHistoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        return new DashboardResponse(
                productRepository.count(),
                productRepository.countByLastCheckStatus(CheckStatus.UP_TO_DATE),
                productRepository.countByLastCheckStatus(CheckStatus.UPDATE_AVAILABLE),
                productRepository.countByLastCheckStatus(CheckStatus.CHECK_FAILED),
                productRepository.countByLastCheckStatus(CheckStatus.UNKNOWN));
    }

    private Product requireProduct(Long id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + id + " was not found."));
    }

    private void validateRequest(ProductRequest request, Long currentId) {
        if (!versionComparator.isValid(request.installedVersion())) {
            throw new InvalidProductException("Installed version must use semantic version syntax.");
        }
        if (request.latestVersion() != null
                && !request.latestVersion().isBlank()
                && !versionComparator.isValid(request.latestVersion())) {
            throw new InvalidProductException("Latest version must use semantic version syntax.");
        }

        boolean duplicate = currentId == null
                ? productRepository.existsByVendorIgnoreCaseAndNameIgnoreCase(
                        request.vendor().trim(), request.name().trim())
                : productRepository.existsByVendorIgnoreCaseAndNameIgnoreCaseAndIdNot(
                        request.vendor().trim(), request.name().trim(), currentId);
        if (duplicate) {
            throw new InvalidProductException("A product with this vendor and name already exists.");
        }

        if (request.checkStrategy() != CheckStrategy.MANUAL) {
            if (request.versionSourceUrl() == null || request.versionSourceUrl().isBlank()) {
                throw new InvalidProductException("A version source URL is required for automatic checks.");
            }
            validateStrategySpecificUrl(request.checkStrategy(), request.versionSourceUrl());
        }
    }

    private void validateStrategySpecificUrl(CheckStrategy strategy, String sourceUrl) {
        URI source = URI.create(sourceUrl.trim());
        if (strategy == CheckStrategy.GITHUB_RELEASES) {
            String host = source.getHost().toLowerCase(Locale.ROOT);
            if (!"github.com".equals(host) && !"api.github.com".equals(host)) {
                throw new InvalidProductException(
                        "GitHub Releases checks require a github.com or api.github.com URL.");
            }
        }
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setVendor(request.vendor().trim());
        product.setInstalledVersion(request.installedVersion().trim());
        product.setLatestVersion(blankToNull(request.latestVersion()));
        product.setVersionSourceUrl(blankToNull(request.versionSourceUrl()));
        product.setCheckStrategy(request.checkStrategy());
        product.setNotes(blankToNull(request.notes()));
        if (product.getLastCheckStatus() == null) {
            product.setLastCheckStatus(CheckStatus.UNKNOWN);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
