package dev.portfolio.releasemonitor.product;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByVendorAscNameAsc();

    List<Product> findByLastCheckStatusOrderByVendorAscNameAsc(CheckStatus status);

    long countByLastCheckStatus(CheckStatus status);

    boolean existsByVendorIgnoreCaseAndNameIgnoreCase(String vendor, String name);

    boolean existsByVendorIgnoreCaseAndNameIgnoreCaseAndIdNot(String vendor, String name, Long id);

    @Query("select product.id from Product product where product.checkStrategy <> :strategy order by product.id")
    List<Long> findIdsByCheckStrategyNot(@Param("strategy") CheckStrategy strategy);
}
