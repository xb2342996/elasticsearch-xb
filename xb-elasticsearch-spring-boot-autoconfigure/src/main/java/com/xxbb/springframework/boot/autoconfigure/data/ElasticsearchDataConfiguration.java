package com.xxbb.springframework.boot.autoconfigure.data;

import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

abstract class ElasticsearchDataConfiguration {
    static class BaseConfiguration {
        @Bean
        @ConditionalOnMissingBean
        ElasticsearchConverter elasticsearchConverter(SimpleElasticsearchMappingContext mappingContext) {
            return new MappingElasticsearchConverter(mappingContext);
        }

        @Bean
        @ConditionalOnMissingBean
        SimpleElasticsearchMappingContext mappingContext() {
            return new SimpleElasticsearchMappingContext();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RestHighLevelClient.class)
    static class RestClientConfiguration {
        @Bean
        @ConditionalOnMissingBean(value = ElasticsearchOperations.class, name = "elasticsearchTemplate")
        @ConditionalOnBean(RestHighLevelClient.class)
        ElasticsearchRestTemplate elasticsearchTemplate(RestHighLevelClient client, ElasticsearchConverter converter) {
            return new ElasticsearchRestTemplate(client, converter);
        }
    }
}
