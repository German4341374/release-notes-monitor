package dev.portfolio.releasemonitor.version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.net.URI;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class GitHubReleaseVersionSourceTest {

    private final GitHubReleaseVersionSource source =
            new GitHubReleaseVersionSource(mock(ResilientHttpClient.class), new ObjectMapper(), "");

    @Test
    void convertsRepositoryUrlToLatestReleaseApi() {
        assertThat(source.toLatestReleaseApiUri("https://github.com/spring-projects/spring-boot"))
                .isEqualTo(URI.create(
                        "https://api.github.com/repos/spring-projects/spring-boot/releases/latest"));
    }

    @Test
    void removesGitSuffix() {
        assertThat(source.toLatestReleaseApiUri("https://github.com/example/project.git"))
                .isEqualTo(URI.create("https://api.github.com/repos/example/project/releases/latest"));
    }

    @Test
    void acceptsApiRepositoryUrl() {
        assertThat(source.toLatestReleaseApiUri("https://api.github.com/repos/example/project"))
                .isEqualTo(URI.create("https://api.github.com/repos/example/project/releases/latest"));
    }

    @Test
    void rejectsLookalikeGithubHost() {
        assertThatThrownBy(
                        () -> source.toLatestReleaseApiUri("https://github.com.example.test/owner/repo"))
                .isInstanceOf(VersionSourceException.class)
                .hasMessageContaining("github.com");
    }
}
