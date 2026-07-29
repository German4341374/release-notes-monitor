package dev.portfolio.releasemonitor.product;

import dev.portfolio.releasemonitor.validation.ValidHttpUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 120) String vendor,
        @NotBlank
                @Size(max = 80)
                @Pattern(
                        regexp = "^[vV]?\\d+(?:\\.\\d+){0,2}(?:-[0-9A-Za-z.-]+)?(?:\\+[0-9A-Za-z.-]+)?$",
                        message = "must be a semantic version")
                String installedVersion,
        @Size(max = 80)
                @Pattern(
                        regexp = "^$|^[vV]?\\d+(?:\\.\\d+){0,2}(?:-[0-9A-Za-z.-]+)?(?:\\+[0-9A-Za-z.-]+)?$",
                        message = "must be empty or a semantic version")
                String latestVersion,
        @Size(max = 500) @ValidHttpUrl String versionSourceUrl,
        @NotNull CheckStrategy checkStrategy,
        @Size(max = 2000) String notes) {}
