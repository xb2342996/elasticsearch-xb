package com.xxbb.springframework.boot.autoconfigure.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

class ElasticsearchRestClientConfigurations {
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(RestClientBuilder.class)
    static class RestClientBuilderConfiguration {
        @Bean
        RestClientBuilderCustomizer defaultRestClientBuilderCustomizer(ElasticsearchRestClientProperites properites) {
            return new DefaultRestClientBuilderCustomizer(properites);
        }

        @Bean
        RestClientBuilder elasticsearchRestClientBuilder(ElasticsearchRestClientProperites properites, ObjectProvider<RestClientBuilderCustomizer> builderCustomizers) {
            HttpHost[] hosts = properites.getUris().stream().map(this::createHttpHost).toArray(HttpHost[]::new);
            RestClientBuilder builder = RestClient.builder(hosts);
            builder.setHttpClientConfigCallback((httpClientBuilder) -> {
                builderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(httpClientBuilder));
                return httpClientBuilder;
            });
            builder.setRequestConfigCallback((requestConfigBuilder) -> {
                builderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
                return requestConfigBuilder;
            });
            builderCustomizers.orderedStream().forEach((customzier) -> customzier.customize(builder));
            return builder;
        }

        private HttpHost createHttpHost(String url) {
            try {
                return createHttpHost(URI.create(url));
            } catch (IllegalArgumentException ex) {
                return HttpHost.create(url);
            }
        }

        private HttpHost createHttpHost(URI uri) {
            if (!StringUtils.hasLength(uri.getUserInfo())) {
                return HttpHost.create(uri.toString());
            }
            try {
                return HttpHost.create(new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment()).toString());
            } catch (URISyntaxException exception) {
                throw new IllegalStateException(exception);
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RestHighLevelClient.class)
    static class RestHighLevelClientConfiguration {
        @Bean
        @ConditionalOnMissingBean
        RestHighLevelClient elasticsearchRestHighLevelClient(RestClientBuilder restClientBuilder) {
            return new RestHighLevelClient(restClientBuilder);
        }

        @Bean
        @ConditionalOnMissingBean
        RestClient elasticsearchClient(RestClientBuilder builder, ObjectProvider<RestHighLevelClient> restHighLevelClient) {
            RestHighLevelClient client = restHighLevelClient.getIfUnique();
            if (client != null) {
                return client.getLowLevelClient();
            }
             return builder.build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class RestClientFallbackConfiguration {
        @Bean
        @ConditionalOnMissingBean
        RestClient elasticsearchRestClient(RestClientBuilder builder) {
            return builder.build();
        }
    }

    static class DefaultRestClientBuilderCustomizer implements RestClientBuilderCustomizer {
        private static final PropertyMapper map = PropertyMapper.get();
        private final ElasticsearchRestClientProperites properites;

        DefaultRestClientBuilderCustomizer(ElasticsearchRestClientProperites properites) {
            this.properites = properites;
        }

        @Override
        public void customize(RestClientBuilder builder) {

        }

        @Override
        public void customize(HttpAsyncClientBuilder builder) {
            builder.setDefaultCredentialsProvider(new PropertiesCredentialsProvider(this.properites));
        }

        @Override
        public void customize(RequestConfig.Builder builder) {
            map.from(this.properites::getConnectionTimeout).whenNonNull().asInt(Duration::toMillis).to(builder::setConnectTimeout);
            map.from(this.properites::getReadTimeout).whenNonNull().asInt(Duration::toMillis).to(builder::setSocketTimeout);
        }
    }

    private static class PropertiesCredentialsProvider extends BasicCredentialsProvider {
        public PropertiesCredentialsProvider(ElasticsearchRestClientProperites properites) {
            if (StringUtils.hasText(properites.getUsername())) {
                Credentials credentials = new UsernamePasswordCredentials(properites.getUsername(), properites.getPassword());
                setCredentials(AuthScope.ANY, credentials);
            }
            properites.getUris().stream().map(this::toUri).filter(this::hasUserInfo).forEach(this::addUserInfoCredentials);
        }

        private URI toUri(String uri) {
            try {
                return URI.create(uri);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        private boolean hasUserInfo(URI uri) {
            return uri != null && StringUtils.hasLength(uri.getUserInfo());
        }

        private void addUserInfoCredentials(URI uri) {
            AuthScope scope = new AuthScope(uri.getHost(), uri.getPort());
            Credentials credentials = createUserInfoCredentials(uri.getUserInfo());
            setCredentials(scope, credentials);
        }

        private Credentials createUserInfoCredentials(String userInfo) {
            int delimiter = userInfo.indexOf(":");
            if (delimiter == -1) {
                return new UsernamePasswordCredentials(userInfo, null);
            }
            String username = userInfo.substring(0, delimiter);
            String password = userInfo.substring(delimiter + 1);
            return new UsernamePasswordCredentials(username, password);
        }
    }
}
