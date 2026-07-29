package dev.portfolio.releasemonitor.api;

import dev.portfolio.releasemonitor.product.CheckHistoryResponse;
import dev.portfolio.releasemonitor.product.CheckStatus;
import dev.portfolio.releasemonitor.product.ProductCheckService;
import dev.portfolio.releasemonitor.product.ProductRequest;
import dev.portfolio.releasemonitor.product.ProductResponse;
import dev.portfolio.releasemonitor.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Tracked product and version-check operations")
public class ProductRestController {

    private final ProductService productService;
    private final ProductCheckService checkService;

    public ProductRestController(ProductService productService, ProductCheckService checkService) {
        this.productService = productService;
        this.checkService = checkService;
    }

    @GetMapping
    @Operation(summary = "List products, optionally filtered by status")
    public List<ProductResponse> list(@RequestParam(required = false) CheckStatus status) {
        return productService.findAll(status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one product")
    public ProductResponse get(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a product")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace an existing product")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product and its check history")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/check")
    @Operation(summary = "Run a version check immediately")
    public ProductResponse check(@PathVariable Long id) {
        return checkService.check(id);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "List the latest 50 checks")
    public List<CheckHistoryResponse> history(@PathVariable Long id) {
        return productService.history(id);
    }
}
