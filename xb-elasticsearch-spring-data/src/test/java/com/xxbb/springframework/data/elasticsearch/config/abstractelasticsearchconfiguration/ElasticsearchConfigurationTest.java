package com.xxbb.springframework.data.elasticsearch.config.abstractelasticsearchconfiguration;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import com.xxbb.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.xxbb.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class ElasticsearchConfigurationTest {

    @Autowired
    private CreateIndexFalseRepository repository;

    @Configuration
    @EnableElasticsearchRepositories(
            basePackages = { "com.xxbb.springframework.data.elasticsearch.config.abstractelasticsearchconfiguration" },
            considerNestedRepositories = true
    )
    static class Config extends AbstractElasticsearchConfiguration {
        @Override
        public RestHighLevelClient elasticsearchClient() {
            return mock(RestHighLevelClient.class);
        }
    }

    @Test
    public void bootstrapRepository() {
        assertThat(repository).isNotNull();
    }

    @Document(indexName = "test-index-config-abstractelasticsearchconfiguraiton", createIndex = false)
    static class CreateIndexFalseEntity {

        @Nullable
        @Id
        private String id;
    }


    interface CreateIndexFalseRepository extends ElasticsearchRepository<CreateIndexFalseEntity, String> {
    }
}
