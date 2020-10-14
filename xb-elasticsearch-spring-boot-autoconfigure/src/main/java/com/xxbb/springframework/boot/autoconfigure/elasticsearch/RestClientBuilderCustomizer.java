package com.xxbb.springframework.boot.autoconfigure.elasticsearch;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClientBuilder;

@FunctionalInterface
public interface RestClientBuilderCustomizer {

    void customize(RestClientBuilder builder);


    default void customize(HttpAsyncClientBuilder builder) {}

    default void customize(RequestConfig.Builder builder) {}
}
