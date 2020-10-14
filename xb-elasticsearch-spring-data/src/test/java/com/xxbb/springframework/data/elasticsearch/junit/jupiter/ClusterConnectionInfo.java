package com.xxbb.springframework.data.elasticsearch.junit.jupiter;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.testcontainers.elasticsearch.ElasticsearchContainer;


public final class ClusterConnectionInfo {
    private final boolean useSsl;
    private final String host;
    private final int httpPort;
    private final int transportPort;
    private final String clusterName;
    @Nullable
    private final ElasticsearchContainer elasticsearchContainer;

	public static Builder builder() {
		return new Builder();
	}

    public ClusterConnectionInfo(boolean useSsl, String host, int httpPort, int transportPort,
                                 @Nullable ElasticsearchContainer elasticsearchContainer) {
        this.useSsl = useSsl;
        this.host = host;
        this.httpPort = httpPort;
        this.transportPort = transportPort;
        this.clusterName = "docker-cluster";
        this.elasticsearchContainer = elasticsearchContainer;
    }

    @Override
    public String toString() {
        return "ClusterConnectionInfo{" +
                "useSsl=" + useSsl +
                ", host='" + host + '\'' +
                ", httpPort=" + httpPort +
                ", transportPort=" + transportPort +
                '}';
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public String getHost() {
        return host;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public int getTransportPort() {
        return transportPort;
    }

    public String getClusterName() {
        return clusterName;
    }

    @Nullable
    public ElasticsearchContainer getElasticsearchContainer() {
        return elasticsearchContainer;
    }

    public static class Builder {
        boolean useSsl = false;
        private String host;
        private int httpPort;
        private int transportPort;
        @Nullable
        private ElasticsearchContainer elasticsearchContainer;

        public Builder useSsl(boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }

        public Builder withHostAndPort(String host, int port) {
            Assert.hasText(host, "host must not be null");
            this.host = host;
            this.httpPort = port;
            return this;
        }

        public Builder withTransportPort(int transportPort) {
            this.transportPort = transportPort;
            return this;
        }
        public Builder withElasticsearchContainer(ElasticsearchContainer elasticsearchContainer) {
            this.elasticsearchContainer = elasticsearchContainer;
            return this;
        }

        public ClusterConnectionInfo build() {
            return new ClusterConnectionInfo(useSsl, host, httpPort, transportPort, elasticsearchContainer);
        }
    }
}
