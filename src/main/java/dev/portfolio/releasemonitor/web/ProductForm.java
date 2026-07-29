package dev.portfolio.releasemonitor.web;

import dev.portfolio.releasemonitor.product.CheckStrategy;
import dev.portfolio.releasemonitor.product.ProductRequest;
import dev.portfolio.releasemonitor.product.ProductResponse;
import dev.portfolio.releasemonitor.validation.ValidHttpUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProductForm {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(max = 120)
    private String vendor;

    @NotBlank
    @Size(max = 80)
    @Pattern(
            regexp = "^[vV]?\\d+(?:\\.\\d+){0,2}(?:-[0-9A-Za-z.-]+)?(?:\\+[0-9A-Za-z.-]+)?$",
            message = "must be a semantic version")
    private String installedVersion;

    @Size(max = 80)
    @Pattern(
            regexp = "^$|^[vV]?\\d+(?:\\.\\d+){0,2}(?:-[0-9A-Za-z.-]+)?(?:\\+[0-9A-Za-z.-]+)?$",
            message = "must be empty or a semantic version")
    private String latestVersion;

    @Size(max = 500)
    @ValidHttpUrl
    private String versionSourceUrl;

    @NotNull
    private CheckStrategy checkStrategy = CheckStrategy.MANUAL;

    @Size(max = 2000)
    private String notes;

    public static ProductForm from(ProductResponse product) {
        ProductForm form = new ProductForm();
        form.name = product.name();
        form.vendor = product.vendor();
        form.installedVersion = product.installedVersion();
        form.latestVersion = product.latestVersion();
        form.versionSourceUrl = product.versionSourceUrl();
        form.checkStrategy = product.checkStrategy();
        form.notes = product.notes();
        return form;
    }

    public ProductRequest toRequest() {
        return new ProductRequest(
                name, vendor, installedVersion, latestVersion, versionSourceUrl, checkStrategy, notes);
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
