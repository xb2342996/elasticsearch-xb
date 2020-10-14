package com.xxbb.springframework.data.elasticsearch.client;

import com.xxbb.springframework.data.elasticsearch.client.ClientConfiguration.TerminalClientConfigurationBuilder;
import com.xxbb.springframework.data.elasticsearch.client.ClientConfiguration.MaybeSecureClientConfigurationBuilder;
import com.xxbb.springframework.data.elasticsearch.client.ClientConfiguration.ClientConfigurationBuilderWithRequiredEndpoint;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ClientConfigurationBuilder implements MaybeSecureClientConfigurationBuilder, ClientConfigurationBuilderWithRequiredEndpoint{
    private final List<InetSocketAddress> hosts = new ArrayList<>();
    private HttpHeaders headers = HttpHeaders.EMPTY;
    private boolean useSsl;
    private @Nullable
    SSLContext sslContext;
    private @Nullable
    HostnameVerifier hostnameVerifier;
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration soTimeout = Duration.ofSeconds(5);
    private @Nullable String username;
    private @Nullable String password;
    private @Nullable String pathPrefix;
    private @Nullable String proxy;
    private Supplier<HttpHeaders> headersSupplier = () -> HttpHeaders.EMPTY;


    @Override
    public MaybeSecureClientConfigurationBuilder connectedTo(String... hostAndPorts) {
        Assert.notNull(hostAndPorts, "At least one host is required");
        this.hosts.addAll(Arrays.stream(hostAndPorts).map(ClientConfigurationBuilder::parse).collect(Collectors.toList()));
        return this;
    }

    @Override
    public MaybeSecureClientConfigurationBuilder connectedTo(InetSocketAddress... endpoints) {
        Assert.notEmpty(endpoints, "At least one host is required");
        this.hosts.addAll(Arrays.asList(endpoints));
        return this;
    }



    @Override
    public TerminalClientConfigurationBuilder usingSsl() {
        this.useSsl = true;
        return this;
    }

    @Override
    public TerminalClientConfigurationBuilder usingSsl(SSLContext context) {
        Assert.notNull(context, "SSL Context must be not null");
        this.useSsl = true;
        this.sslContext = context;
        return this;
    }

    @Override
    public TerminalClientConfigurationBuilder usingSsl(SSLContext context, HostnameVerifier hostnameVerifier) {
        Assert.notNull(context, "SSL Context must be not null");
        Assert.notNull(hostnameVerifier, "Host name Verifier must be not null");
        this.sslContext = context;
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }


    @Override
    public TerminalClientConfigurationBuilder withDefaultHeaders(HttpHeaders defaultHeaders) {
        Assert.notNull(defaultHeaders, "Default httpheaders must be not null");
        this.headers = new HttpHeaders();
        this.headers.addAll(defaultHeaders);
        return this;
    }

    @Override
    public TerminalClientConfigurationBuilder withConnectionTimeout(Duration duration) {
        Assert.notNull(duration, "Duration must be not null");
        this.connectTimeout = duration;
        return this;
    }

    @Override
    public TerminalClientConfigurationBuilder withSocketTimeout(Duration duration) {
        Assert.notNull(duration, "Duration must be not null");
        this.soTimeout = duration;
        return this;
    }

    @Override
    public TerminalClientConfigurationBuilder withBasicAuth(String username, String password) {
        Assert.notNull(username, "username must not be null");
        Assert.notNull(username, "password must not be null");
        this.username = username;
        this.password = password;
        return this;
    }

    @Override
    public TerminalClientConfigurationBuilder withPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
        return this;
    }

    @Override
    public MaybeSecureClientConfigurationBuilder withProxy(String proxy) {
        Assert.hasLength(proxy, "Proxy cannot be empty or null");
        this.proxy = proxy;
        return this;
    }

    @Override
    public TerminalClientConfigurationBuilder withHeaders(Supplier<HttpHeaders> headers) {
        Assert.notNull(headers, "HeaderSupplier must not be null");
        this.headersSupplier = headers;
        return this;
    }


    @Override
    public ClientConfiguration build() {
        if (username != null && password != null) {
            if (HttpHeaders.EMPTY.equals(headers)) {
                headers = new HttpHeaders();
            }
            headers.setBasicAuth(username, password);
        }
        return new DefaultClientConfiguration(hosts, headers, useSsl, sslContext, soTimeout, connectTimeout, pathPrefix, hostnameVerifier, proxy, headersSupplier);
    }

    private static InetSocketAddress parse(String hostAndPort) {
        return InetSocketAddressParser.parse(hostAndPort, ElasticsearchHost.DEFAULT_HOST);
    }
}
