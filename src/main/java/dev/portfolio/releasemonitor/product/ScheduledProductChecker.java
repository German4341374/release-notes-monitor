package dev.portfolio.releasemonitor.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "release-monitor.scheduling.enabled",
        havingValue = "true")
public class ScheduledProductChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledProductChecker.class);

    private final ProductRepository productRepository;
    private final ProductCheckService checkService;

    public ScheduledProductChecker(
            ProductRepository productRepository, ProductCheckService checkService) {
        this.productRepository = productRepository;
        this.checkService = checkService;
    }

    @Scheduled(
            cron = "${release-monitor.scheduling.cron:0 0 */6 * * *}",
            zone = "${release-monitor.scheduling.zone:UTC}")
    public void checkAutomaticSources() {
        for (Long productId : productRepository.findIdsByCheckStrategyNot(CheckStrategy.MANUAL)) {
            try {
                ProductResponse result = checkService.check(productId);
                LOGGER.info(
                        "Scheduled version check completed productId={} status={}",
                        productId,
                        result.lastCheckStatus().name());
            } catch (RuntimeException exception) {
                LOGGER.warn(
                        "Scheduled version check could not be completed productId={} errorType={}",
                        productId,
                        exception.getClass().getSimpleName());
            }
        }
    }
}
