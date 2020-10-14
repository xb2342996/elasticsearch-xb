package com.xxbb.springframework.data.elasticsearch.repositories.custommethod;

import com.xxbb.springframework.data.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.xxbb.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = { CustomMethodRepositoryRestTests.Config.class })
public class CustomMethodRepositoryRestTests extends CustomMethodRepositoryBaseTests {
    @Configuration
    @Import({ElasticsearchRestTemplateConfiguration.class})
    @EnableElasticsearchRepositories(
            basePackages = {"com.xxbb.springframework.data.elasticsearch.repositories.custommethod"},
            considerNestedRepositories = true
    )
    static class Config{}
}
