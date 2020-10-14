package com.xxbb.springframework.data.elasticsearch.client;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DefaultClientConfiguration implements ClientConfiguration{

    private final List<InetSocketAddress> hosts;
    private final HttpHeaders headers;
    private final boolean useSsl;
    private final @Nullable SSLContext sslContext;
    private final Duration soTimeout;
    private final Duration connectTimeout;
    private final @Nullable String pathPrefix;
    private final @Nullable HostnameVerifier hostnameVerifier;
    private final @Nullable String proxy;
    private final Supplier<HttpHeaders> headersSupplier;

    public DefaultClientConfiguration(List<InetSocketAddress> hosts, HttpHeaders headers, boolean useSsl,
                                      @Nullable SSLContext sslContext, Duration soTimeout, Duration connectTimeout,
                                      @Nullable String pathPrefix, @Nullable HostnameVerifier hostnameVerifier,
                                      @Nullable String proxy, Supplier<HttpHeaders> headersSupplier) {
        this.hosts = Collections.unmodifiableList(new ArrayList<>(hosts));
        this.headers = new HttpHeaders(headers);
        this.useSsl = useSsl;
        this.sslContext = sslContext;
        this.soTimeout = soTimeout;
        this.connectTimeout = connectTimeout;
        this.pathPrefix = pathPrefix;
        this.hostnameVerifier = hostnameVerifier;
        this.proxy = proxy;
        this.headersSupplier = headersSupplier;
    }

    @Override
    public List<InetSocketAddress> getEndpoints() {
        return this.hosts;
    }

    @Override
    public HttpHeaders getDefaultHeaders() {
        return this.headers;
    }

    @Override
    public boolean useSsl() {
        return this.useSsl;
    }

    @Override
    public Optional<SSLContext> getSslContext() {
        return Optional.ofNullable(sslContext);
    }

    @Override
    public Optional<HostnameVerifier> getHostnameVerifier() {
        return Optional.ofNullable(hostnameVerifier);
    }

    @Override
    public Duration getConnectTimeout() {
        return this.connectTimeout;
    }

    @Override
    public Duration getSocketTimeout() {
        return this.soTimeout;
    }

    @Nullable
    @Override
    public String getPathPrefix() {
        return this.pathPrefix;
    }

    @Override
    public Optional<String> getProxy() {
        return Optional.ofNullable(proxy);
    }

    @Override
    public Supplier<HttpHeaders> getHeadersSupplier() {
        return headersSupplier;
    }
}
