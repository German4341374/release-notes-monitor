package dev.portfolio.releasemonitor.version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SemanticVersionComparatorTest {

    private SemanticVersionComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new SemanticVersionComparator();
    }

    @Test
    void detectsMajorUpgrade() {
        assertThat(comparator.isUpdateAvailable("1.9.9", "2.0.0")).isTrue();
    }

    @Test
    void detectsMinorUpgrade() {
        assertThat(comparator.isUpdateAvailable("2.4.9", "2.5.0")).isTrue();
    }

    @Test
    void detectsPatchUpgrade() {
        assertThat(comparator.isUpdateAvailable("3.1.4", "3.1.5")).isTrue();
    }

    @Test
    void treatsEqualVersionsAsCurrent() {
        assertThat(comparator.compare("1.2.3", "1.2.3")).isZero();
    }

    @Test
    void acceptsLeadingVPrefix() {
        assertThat(comparator.compare("v4.1.0", "4.1.0")).isZero();
    }

    @Test
    void treatsMissingMinorAndPatchAsZero() {
        assertThat(comparator.compare("2", "2.0.0")).isZero();
    }

    @Test
    void ignoresBuildMetadata() {
        assertThat(comparator.compare("1.0.0+linux.1", "1.0.0+windows.9")).isZero();
    }

    @Test
    void stableVersionIsNewerThanPreRelease() {
        assertThat(comparator.compare("1.0.0", "1.0.0-rc.1")).isPositive();
    }

    @Test
    void comparesNumericPreReleaseIdentifiersNumerically() {
        assertThat(comparator.compare("1.0.0-rc.10", "1.0.0-rc.2")).isPositive();
    }

    @Test
    void numericPreReleaseIdentifierSortsBeforeTextIdentifier() {
        assertThat(comparator.compare("1.0.0-1", "1.0.0-alpha")).isNegative();
    }

    @Test
    void supportsVersionNumbersLargerThanLong() {
        assertThat(comparator.compare("999999999999999999999.0.0", "2.0.0")).isPositive();
    }

    @Test
    void rejectsInvalidVersion() {
        assertThatThrownBy(() -> comparator.compare("release-one", "1.0.0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("semantic version");
    }
}
