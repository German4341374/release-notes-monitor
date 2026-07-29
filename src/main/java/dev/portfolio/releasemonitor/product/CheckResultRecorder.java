package dev.portfolio.releasemonitor.product;

import dev.portfolio.releasemonitor.error.ResourceNotFoundException;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckResultRecorder {

    private final ProductRepository productRepository;
    private final ProductCheckHistoryRepository historyRepository;

    public CheckResultRecorder(
            ProductRepository productRepository, ProductCheckHistoryRepository historyRepository) {
        this.productRepository = productRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public ProductResponse record(
            Long productId, String detectedVersion, CheckStatus status, String message) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " was not found."));
        Instant checkedAt = Instant.now();
        if (detectedVersion != null && !detectedVersion.isBlank()) {
            product.setLatestVersion(detectedVersion);
        }
        product.setLastCheckedAt(checkedAt);
        product.setLastCheckStatus(status);
        productRepository.save(product);
        historyRepository.save(new ProductCheckHistory(
                product,
                checkedAt,
                product.getInstalledVersion(),
                detectedVersion,
                status,
                truncate(message)));
        return ProductResponse.from(product);
    }

    private String truncate(String message) {
        String safeMessage = message == null || message.isBlank() ? "Version check completed." : message;
        return safeMessage.length() <= 500 ? safeMessage : safeMessage.substring(0, 500);
    }
}
