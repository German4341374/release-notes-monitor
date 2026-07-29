package dev.portfolio.releasemonitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    @Bean
    OpenAPI releaseMonitorOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Release Notes Monitor API")
                        .version("v1")
                        .description("Track installed versions and check supported release sources."));
    }
}
