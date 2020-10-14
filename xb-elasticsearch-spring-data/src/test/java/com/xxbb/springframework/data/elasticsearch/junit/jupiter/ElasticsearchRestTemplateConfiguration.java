package com.xxbb.springframework.data.elasticsearch.junit.jupiter;

import com.xxbb.springframework.data.elasticsearch.client.ClientConfiguration;
import com.xxbb.springframework.data.elasticsearch.client.RestClients;
import com.xxbb.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;

import javax.annotation.Resource;
import java.time.Duration;

@Configuration
public class ElasticsearchRestTemplateConfiguration extends AbstractElasticsearchConfiguration {
    @Autowired
    private ClusterConnectionInfo clusterConnectionInfo;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        String elasticsearchHostPort = clusterConnectionInfo.getHost() + ":" + clusterConnectionInfo.getHttpPort();

        ClientConfiguration.TerminalClientConfigurationBuilder configurationBuilder = ClientConfiguration.builder().connectedTo(elasticsearchHostPort);

        if (clusterConnectionInfo.isUseSsl()) {
            configurationBuilder = ((ClientConfiguration.MaybeSecureClientConfigurationBuilder) configurationBuilder).usingSsl();
        }
        return RestClients.create(configurationBuilder
                .withConnectionTimeout(Duration.ofSeconds(20))
                .withSocketTimeout(Duration.ofSeconds(20)).build()).rest();
    }

    @Override
    public ElasticsearchOperations elasticsearchOperations(ElasticsearchConverter elasticsearchConverter) {
        RestHighLevelClient client = elasticsearchClient();
        return new ElasticsearchRestTemplate(client, elasticsearchConverter) {

            @Override
            public <T> T execute(ClientCallback<T> callback) {
                try {
                    return super.execute(callback);
                } catch (DataAccessResourceFailureException e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {

                    }
                    return super.execute(callback);
                }
            }
        };
    }
}
