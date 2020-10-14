package com.xxbb.springframework.data.elasticsearch.client;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ClusterNodeUnitTest {
    @Test
    public void parsesSingleCluterNode() {
        ClusterNodes ndoes = ClusterNodes.DEFAULT;
        assertThat(ndoes).hasSize(1).first().satisfies(it -> {
            assertThat(it.getAddress()).isEqualTo("127.0.0.1");
            assertThat(it.getPort()).isEqualTo(9200);
        });
    }

    @Test
    public void parseMultiClusterNodes() {
        ClusterNodes nodes = ClusterNodes.of("127.0.0.1:9300,172.16.68.128:9200");

        assertThat(nodes.stream()).element(0).satisfies(it -> {
            assertThat(it.getAddress()).isEqualTo("127.0.0.1");
            assertThat(it.getPort()).isEqualTo(9300);
        });
        assertThat(nodes.stream()).element(1).satisfies(it -> {
            assertThat(it.getAddress()).isEqualTo("172.16.68.128");
            assertThat(it.getPort()).isEqualTo(9200);
        });
    }

    @Test
    public void rejectEmptyHostName() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ClusterNodes.of(":8080")).withMessageContaining("host");
    }

    @Test
    public void rejectEmptyPortNumber() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ClusterNodes.of("127.0.0.1:")).withMessageContaining("port");
    }

    @Test
    public void rejectMissingPort() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ClusterNodes.of("127.0.0.1")).withMessageContaining("host:port");
    }

    @Test
    public void rejectUnresolvedHost() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ClusterNodes.of("invalidhost.myip:8080"));
    }
}
