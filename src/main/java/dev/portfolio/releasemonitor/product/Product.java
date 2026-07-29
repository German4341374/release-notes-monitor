package dev.portfolio.releasemonitor.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
        name = "products",
        uniqueConstraints = @UniqueConstraint(name = "uq_products_vendor_name", columnNames = {"vendor", "name"}))
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String vendor;

    @Column(name = "installed_version", nullable = false, length = 80)
    private String installedVersion;

    @Column(name = "latest_version", length = 80)
    private String latestVersion;

    @Column(name = "version_source_url", length = 500)
    private String versionSourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_strategy", nullable = false, length = 40)
    private CheckStrategy checkStrategy;

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_check_status", nullable = false, length = 40)
    private CheckStatus lastCheckStatus = CheckStatus.UNKNOWN;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (lastCheckStatus == null) {
            lastCheckStatus = CheckStatus.UNKNOWN;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getInstalledVersion() {
        return installedVersion;
    }

    public void setInstalledVersion(String installedVersion) {
        this.installedVersion = installedVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getVersionSourceUrl() {
        return versionSourceUrl;
    }

    public void setVersionSourceUrl(String versionSourceUrl) {
        this.versionSourceUrl = versionSourceUrl;
    }

    public CheckStrategy getCheckStrategy() {
        return checkStrategy;
    }

    public void setCheckStrategy(CheckStrategy checkStrategy) {
        this.checkStrategy = checkStrategy;
    }

    public Instant getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(Instant lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public CheckStatus getLastCheckStatus() {
        return lastCheckStatus;
    }

    public void setLastCheckStatus(CheckStatus lastCheckStatus) {
        this.lastCheckStatus = lastCheckStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
