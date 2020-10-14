package com.xxbb.springframework.data.elasticsearch.client;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface ClientConfiguration {
    static ClientConfigurationBuilderWithRequiredEndpoint builder() {
        return new ClientConfigurationBuilder();
    }

    static ClientConfiguration localhost() {
        return new ClientConfigurationBuilder().connectedToLocalhost().build();
    }

    static ClientConfiguration create(String hostAndPort) {
        return new ClientConfigurationBuilder().connectedTo(hostAndPort).build();
    }

    static ClientConfiguration create(InetSocketAddress address) {
        return new ClientConfigurationBuilder().connectedTo(address).build();
    }

    List<InetSocketAddress> getEndpoints();

    HttpHeaders getDefaultHeaders();

    boolean useSsl();

    Optional<SSLContext> getSslContext();

    Optional<HostnameVerifier> getHostnameVerifier();

    Duration getConnectTimeout();

    Duration getSocketTimeout();

    @Nullable
    String getPathPrefix();

    Optional<String> getProxy();

    Supplier<HttpHeaders> getHeadersSupplier();

    interface ClientConfigurationBuilderWithRequiredEndpoint {
        default MaybeSecureClientConfigurationBuilder connectedTo(String hostAndPort) {
            return connectedTo(new String[] {hostAndPort});
        }

        MaybeSecureClientConfigurationBuilder connectedTo(String... hostAndPorts);

        default MaybeSecureClientConfigurationBuilder connectedTo(InetSocketAddress endpoint) {
            return connectedTo(new InetSocketAddress[] {endpoint});
        }

        MaybeSecureClientConfigurationBuilder connectedTo(InetSocketAddress... endpoints);

        default MaybeSecureClientConfigurationBuilder connectedToLocalhost() {
            return connectedTo("localhost:9200");
        }
    }

    interface MaybeSecureClientConfigurationBuilder extends TerminalClientConfigurationBuilder{
        TerminalClientConfigurationBuilder usingSsl();

        TerminalClientConfigurationBuilder usingSsl(SSLContext context);

        TerminalClientConfigurationBuilder usingSsl(SSLContext context, HostnameVerifier hostnameVerifier);
    }

    interface TerminalClientConfigurationBuilder {
        TerminalClientConfigurationBuilder withDefaultHeaders(HttpHeaders defaultHeaders);

        default TerminalClientConfigurationBuilder withConnectionTimeout(long millis) {
            return withConnectionTimeout(Duration.ofMillis(millis));
        }

        TerminalClientConfigurationBuilder withConnectionTimeout(Duration duration);

        default TerminalClientConfigurationBuilder withSocketTimeout(long millis) {
            return withConnectionTimeout(Duration.ofMillis(millis));
        }

        TerminalClientConfigurationBuilder withSocketTimeout(Duration duration);

        TerminalClientConfigurationBuilder withBasicAuth(String username, String password);

        TerminalClientConfigurationBuilder withPathPrefix(String pathPrefix);

        TerminalClientConfigurationBuilder withProxy(String proxy);

        TerminalClientConfigurationBuilder withHeaders(Supplier<HttpHeaders> headers);

        ClientConfiguration build();
    }
}
