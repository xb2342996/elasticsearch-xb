package com.xxbb.springframework.data.elasticsearch.client;


import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class RestClients {
    private static final String LOG_ID_ATTRIBUTE = RestClients.class.getName() + ".LOG_ID";

    private RestClients() {
    }

    public static ElasticsearchRestClient create(ClientConfiguration clientConfiguration) {
        Assert.notNull(clientConfiguration, "ClientConfiguration must not be null");
        HttpHost[] httpHosts = formattedHosts(clientConfiguration.getEndpoints(), clientConfiguration.useSsl()).stream().map(HttpHost::create).toArray(HttpHost[]::new);
        RestClientBuilder builder = RestClient.builder(httpHosts);
        if (clientConfiguration.getPathPrefix() != null) {
            builder.setPathPrefix(clientConfiguration.getPathPrefix());
        }

        HttpHeaders headers = clientConfiguration.getDefaultHeaders();

        if (!headers.isEmpty()) {
            builder.setDefaultHeaders(toHeadArray(headers));
        }

        builder.setHttpClientConfigCallback(clientBuilder -> {
            clientConfiguration.getSslContext().ifPresent(clientBuilder::setSSLContext);
            clientConfiguration.getHostnameVerifier().ifPresent(clientBuilder::setSSLHostnameVerifier);

            // set interceptor
            clientBuilder.addInterceptorLast(new CustomHeaderInjector(clientConfiguration.getHeadersSupplier()));
            if (ClientLogger.isEnable()) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                clientBuilder.addInterceptorLast((HttpRequestInterceptor) interceptor);
                clientBuilder.addInterceptorLast((HttpResponseInterceptor) interceptor);
            }

            Builder requestConfigBuilder = RequestConfig.custom();
            Duration connectTimout = clientConfiguration.getConnectTimeout();

            if (!connectTimout.isNegative()) {
                requestConfigBuilder.setConnectTimeout(Math.toIntExact(connectTimout.toMillis()));
                requestConfigBuilder.setConnectionRequestTimeout(Math.toIntExact(connectTimout.toMillis()));
            }

            Duration soTimeout = clientConfiguration.getSocketTimeout();
            if (!soTimeout.isNegative()) {
                requestConfigBuilder.setSocketTimeout(Math.toIntExact(soTimeout.toMillis()));
            }

            clientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
            clientConfiguration.getProxy().map(HttpHost::create).ifPresent(clientBuilder::setProxy);
            return clientBuilder;
        });
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return () -> client;
    }

    private static Header[] toHeadArray(HttpHeaders headers) {
        return headers.entrySet().stream().flatMap(entry -> entry.getValue().stream().map(value -> new BasicHeader(entry.getKey(), value))).toArray(Header[]::new);
    }

    private static List<String> formattedHosts(List<InetSocketAddress> hosts, boolean useSsl) {
        return hosts.stream().map(it -> (useSsl ? "https" : "http" + "://" + it.getHostName() + ":" + it.getPort())).collect(Collectors.toList());
    }

    public interface ElasticsearchRestClient extends Closeable {
        RestHighLevelClient rest();

        default RestClient lowLevelClient() {
            return rest().getLowLevelClient();
        }

        @Override
        default void close() throws IOException {
            rest().close();
        }
    }

    private static class HttpLoggingInterceptor implements HttpResponseInterceptor, HttpRequestInterceptor {

        @Override
        public void process(HttpRequest httpRequest, HttpContext httpContext) throws IOException {
            String logId = (String) httpContext.getAttribute(RestClients.LOG_ID_ATTRIBUTE);
            if (logId == null) {
                logId = ClientLogger.newLogId();
                httpContext.setAttribute(RestClients.LOG_ID_ATTRIBUTE, logId);
            }
            if (httpRequest instanceof HttpEntityEnclosingRequest && ((HttpEntityEnclosingRequest) httpRequest).getEntity() != null) {
                HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
                HttpEntity entity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                entity.writeTo(buffer);

                if (!entity.isRepeatable()) {
                    entityEnclosingRequest.setEntity(new ByteArrayEntity(buffer.toByteArray()));
                }

                ClientLogger.logRequest(logId, httpRequest.getRequestLine().getMethod(), httpRequest.getRequestLine().getUri(), "", () -> new String(buffer.toByteArray()));
            } else {
                ClientLogger.logRequest(logId, httpRequest.getRequestLine().getMethod(), httpRequest.getRequestLine().getUri(), "");
            }
        }

        @Override
        public void process(HttpResponse httpResponse, HttpContext httpContext) {
            String logId = (String) httpContext.getAttribute(RestClients.LOG_ID_ATTRIBUTE);
            ClientLogger.logRawResponse(logId, HttpStatus.resolve(httpResponse.getStatusLine().getStatusCode()));
        }
    }


    private static class CustomHeaderInjector implements HttpRequestInterceptor {
        private final Supplier<HttpHeaders> headersSupplier;

        public CustomHeaderInjector(Supplier<HttpHeaders> headersSupplier) {
            this.headersSupplier = headersSupplier;
        }

        @Override
        public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
            HttpHeaders httpHeaders = headersSupplier.get();

            if (httpHeaders != null && httpHeaders != HttpHeaders.EMPTY) {
                Arrays.stream(toHeadArray(httpHeaders)).forEach(httpRequest::addHeader);
            }
        }
    }
}
