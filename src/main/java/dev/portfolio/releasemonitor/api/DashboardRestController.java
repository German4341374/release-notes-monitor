package dev.portfolio.releasemonitor.api;

import dev.portfolio.releasemonitor.product.DashboardResponse;
import dev.portfolio.releasemonitor.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Aggregate release monitoring metrics")
public class DashboardRestController {

    private final ProductService productService;

    public DashboardRestController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get dashboard counters")
    public DashboardResponse dashboard() {
        return productService.dashboard();
    }
}
