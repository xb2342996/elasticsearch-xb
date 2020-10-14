package com.xxbb.springframework.data.elasticsearch.config;

import com.xxbb.springframework.data.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.SpringIntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.ContextConfiguration;

@SpringIntegrationTest
@ContextConfiguration(classes = { ElasticsearchRestAuditingIntegrationTest.Config.class })
public class ElasticsearchRestAuditingIntegrationTest extends AuditingIntegrationTest {

    @Import({ ElasticsearchRestTemplateConfiguration.class })
    @EnableElasticsearchAuditing(auditorAwareRef = "auditorAware")
    static class Config {
        @Bean
        public AuditorAware<String> auditorAware() {
            return auditorProvider();
        }
    }
}
