package com.xxbb.springframework.boot.autoconfigure.elasticsearch;

import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RestClient.class)
@EnableConfigurationProperties(ElasticsearchRestClientProperites.class)
@Import({ ElasticsearchRestClientConfigurations.RestClientBuilderConfiguration.class,
        ElasticsearchRestClientConfigurations.RestClientFallbackConfiguration.class,
        ElasticsearchRestClientConfigurations.RestHighLevelClientConfiguration.class})
public class ElasticsearchRestClientAutoConfiguration {
}
