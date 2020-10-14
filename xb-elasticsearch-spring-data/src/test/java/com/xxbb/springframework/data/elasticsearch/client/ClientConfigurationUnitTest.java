package com.xxbb.springframework.data.elasticsearch.client;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicAuthCache;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

import javax.net.ssl.SSLContext;
import javax.security.auth.login.Configuration;
import java.net.InetSocketAddress;
import java.time.Duration;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


public class ClientConfigurationUnitTest {

    private static final String hostAndPort = "172.16.68.128:9200";
    @Test
    public void shouldCreateSimpleConfiguration() {
        ClientConfiguration configuration = ClientConfiguration.create(hostAndPort);
        assertThat(configuration.getEndpoints()).containsOnly(InetSocketAddress.createUnresolved("172.16.68.128", 9200));
    }

    @Test
    public void shouldCreateCustomizedConfiguration() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("foo", "bar");
        ClientConfiguration configuration = ClientConfiguration.builder()
                .connectedTo("foo", "bar")
                .usingSsl()
                .withSocketTimeout(Duration.ofDays(2)).withConnectionTimeout(Duration.ofDays(1))
                .withDefaultHeaders(headers)
                .withPathPrefix("myPathPrefix")
                .withProxy(hostAndPort).build();
        assertThat(configuration.getEndpoints()).containsOnly(InetSocketAddress.createUnresolved("foo", 9200), InetSocketAddress.createUnresolved("bar", 9200));
        assertThat(configuration.useSsl()).isTrue();
        assertThat(configuration.getSocketTimeout()).isEqualTo(Duration.ofDays(2));
        assertThat(configuration.getConnectTimeout()).isEqualTo(Duration.ofDays(1));
        assertThat(configuration.getDefaultHeaders().get("foo")).containsOnly("bar");
        assertThat(configuration.getProxy()).contains(hostAndPort);
    }

    @Test
    public void shouldCreateSSLConfiguration() {
        SSLContext sslContext = mock(SSLContext.class);

        ClientConfiguration configuration = ClientConfiguration.builder().connectedTo("foo", "bar")
                .usingSsl(sslContext)
                .build();
        assertThat(configuration.getEndpoints()).containsOnly(InetSocketAddress.createUnresolved("foo", 9200), InetSocketAddress.createUnresolved("bar", 9200));
        assertThat(configuration.useSsl()).isTrue();
        assertThat(configuration.getSslContext()).contains(sslContext);
        assertThat(configuration.getSocketTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(configuration.getConnectTimeout()).isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    public void shouldAddBasicAuthenticationHeaderWhenNoHeadersAreSet() {
        String username = "username";
        String password = "password";
        ClientConfiguration configuration = ClientConfiguration.builder().connectedTo("foo", "bar")
                .withBasicAuth(username, password)
                .build();

        assertThat(configuration.getDefaultHeaders().get(HttpHeaders.AUTHORIZATION)).containsOnly(buildBasicAuth(username, password));
    }

    @Test
    public void shouldAddBasicAuthenticationHeaderAndKeepHeaders() {
        String username = "username";
        String password = "password";

        HttpHeaders headers = new HttpHeaders();
        headers.set("foo", "bar");
        ClientConfiguration configuration = ClientConfiguration.builder().connectedTo("foo", "bar")
                .withDefaultHeaders(headers)
                .withBasicAuth(username, password)
                .build();
        HttpHeaders defaultHeader = configuration.getDefaultHeaders();
        assertThat(defaultHeader.get(HttpHeaders.AUTHORIZATION)).containsOnly(buildBasicAuth(username, password));
        assertThat(defaultHeader.getFirst("foo")).isEqualTo("bar");
        assertThat(headers.get(HttpHeaders.AUTHORIZATION)).isNull();
    }

    @Test
    public void shouldCreateSslConfigurationWithHostnameVerifier() {
        SSLContext sslContext = mock(SSLContext.class);
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("foo", "bar")
                .usingSsl(sslContext, NoopHostnameVerifier.INSTANCE)
                .build();
        assertThat(clientConfiguration.getHostnameVerifier()).contains(NoopHostnameVerifier.INSTANCE);
    }

    private static String buildBasicAuth(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        return headers.getFirst(HttpHeaders.AUTHORIZATION);
    }
}