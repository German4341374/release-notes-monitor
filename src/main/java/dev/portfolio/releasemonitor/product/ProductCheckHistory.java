package dev.portfolio.releasemonitor.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "product_check_history")
public class ProductCheckHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @Column(name = "installed_version", nullable = false, length = 80)
    private String installedVersion;

    @Column(name = "detected_version", length = 80)
    private String detectedVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CheckStatus status;

    @Column(nullable = false, length = 500)
    private String message;

    protected ProductCheckHistory() {}

    public ProductCheckHistory(
            Product product,
            Instant checkedAt,
            String installedVersion,
            String detectedVersion,
            CheckStatus status,
            String message) {
        this.product = product;
        this.checkedAt = checkedAt;
        this.installedVersion = installedVersion;
        this.detectedVersion = detectedVersion;
        this.status = status;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public String getInstalledVersion() {
        return installedVersion;
    }

    public String getDetectedVersion() {
        return detectedVersion;
    }

    public CheckStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
