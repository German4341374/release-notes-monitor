package dev.portfolio.releasemonitor.product;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCheckHistoryRepository extends JpaRepository<ProductCheckHistory, Long> {

    List<ProductCheckHistory> findTop50ByProductIdOrderByCheckedAtDesc(Long productId);
}
