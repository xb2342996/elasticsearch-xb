package com.xxbb.springframework.data.elasticsearch.config;

import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;

public abstract class AbstractElasticsearchConfiguration extends ElasticsearchConfigurationSupport {

    public abstract RestHighLevelClient elasticsearchClient();

    @Bean(name = { "elasticsearchOperations", "elasticsearchTemplate" })
    public ElasticsearchOperations elasticsearchOperations(ElasticsearchConverter elasticsearchConverter) {
        return new ElasticsearchRestTemplate(elasticsearchClient(), elasticsearchConverter);
    }
}
