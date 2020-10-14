package com.xxbb.springframework.data.elasticsearch.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;

import javax.security.auth.login.Configuration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

//@Disabled("SocketException: Socket closed happens on the CLI build while running the test individually succeeds")
public class RestClientUnitTest {

    @ParameterizedTest
    @MethodSource("clientUnderTestFactorySource")
    @DisplayName("should use configured proxy")
    void shouldUseConfiguredProxy(ClientUnderTestFactory factory) {
        wireMockServer(server -> {
            WireMock.configureFor(server.port());
            stubFor(head(urlEqualTo("/")).willReturn(aResponse().withHeader("Content-Type", "application/json; charset=UTF-8")));

            ClientConfiguration configuration =new ClientConfigurationBuilder().connectedTo("127.16.68.128:9200")
                    .withProxy("localhost:" + server.port()).build();
            ClientUnderTest clientUnderTest = factory.create(configuration);
            clientUnderTest.ping();

            verify(headRequestedFor(urlEqualTo("/")));
        });
    }

    @ParameterizedTest
    @MethodSource("clientUnderTestFactorySource")
    @DisplayName("should set all required headers")
    void shouldSetAllRequiredHeaders(ClientUnderTestFactory factory) {
        wireMockServer(server -> {
            WireMock.configureFor(server.port());
            stubFor(head(urlEqualTo("/")).willReturn(aResponse().withHeader("Content-Type", "application/json; charset=UTF-8")));

            HttpHeaders headers = new HttpHeaders();
            headers.addAll("def1", Arrays.asList("def1-1", "def1-2"));
            headers.add("def2", "def2-1");

            AtomicInteger supplierCount = new AtomicInteger(1);
            ClientConfiguration configuration = new ClientConfigurationBuilder().connectedTo("localhost:" + server.port())
                    .withBasicAuth("username", "password")
                    .withDefaultHeaders(headers)
                    .withHeaders(() -> {
                        HttpHeaders httpHeaders = new HttpHeaders();
                        httpHeaders.add("supplied", "val0");
                        httpHeaders.add("supplied", "val" + supplierCount.getAndDecrement());
                        return httpHeaders;
                    })
                    .build();

            ClientUnderTest clientUnderTest = factory.create(configuration);

            for (int i = 1; i <= 3; i++) {
                clientUnderTest.ping();
                verify(headRequestedFor(urlEqualTo("/")).withHeader("Authorization", new AnythingPattern())
                        .withHeader("def1", new EqualToPattern("def1-1"))
                        .withHeader("def1", new EqualToPattern("def1-2"))
                        .withHeader("def2", new EqualToPattern("def2-1"))
//                        .withHeader("supplied", new EqualToPattern("val0"))
//                        .withHeader("supplied", new EqualToPattern("val" + i))
                );
            }
        });
    }

    @FunctionalInterface
    interface WiremockConsumer extends Consumer<WireMockServer> {
        @Override
        default void accept(WireMockServer wireMockServer) {
            try {
                acceptThrows(wireMockServer);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        void acceptThrows(WireMockServer consumer) throws Exception;
    }

    private void wireMockServer(WiremockConsumer consumer) {
        WireMockServer wireMockServer = new WireMockServer(options()
        .dynamicPort().usingFilesUnderDirectory("src/test/resources/wiremock-mappings"));

        try {
            wireMockServer.start();
            consumer.accept(wireMockServer);
        } finally {
            wireMockServer.shutdown();
        }
    }

    interface ClientUnderTest{
        boolean ping() throws Exception;
    }
    static abstract class ClientUnderTestFactory {
        abstract ClientUnderTest create(ClientConfiguration configuration);

        @Override
        public String toString() {
            return getDisplayName();
        }
        protected abstract String getDisplayName();
    }

    static class RestClientUnderClientTestFactory extends ClientUnderTestFactory {

        @Override
        ClientUnderTest create(ClientConfiguration configuration) {
            RestHighLevelClient client = RestClients.create(configuration).rest();
            return new ClientUnderTest() {
                @Override
                public boolean ping() throws Exception {
                    return client.ping(RequestOptions.DEFAULT);
                }
            };
        }

        @Override
        protected String getDisplayName() {
            return "restClientHighLevel";
        }
    }

    static Stream<ClientUnderTestFactory> clientUnderTestFactorySource() {
        return Stream.of(new RestClientUnderClientTestFactory());
    }
}
