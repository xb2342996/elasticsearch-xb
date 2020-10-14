package com.xxbb.springframework.data.elasticsearch.junit.jupiter;

import com.xxbb.springframework.data.elasticsearch.support.VersionInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.net.MalformedURLException;
import java.net.URL;

public class ClusterConnection implements ExtensionContext.Store.CloseableResource {
    private static final Logger logger = LoggerFactory.getLogger(ClusterConnection.class);

    private static final int ELASTICSEARCH_DEFAULT_PORT = 9200;
    private static final int ELASTICSEARCH_DEFAULT_TRANSPORT_PORT = 9300;
    private static final String ELASTICSEARCH_DEFAULT_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch";

    private static final ThreadLocal<ClusterConnectionInfo> clusterConnectionInfoThreadLocal = new ThreadLocal<>();

    @Nullable private final ClusterConnectionInfo clusterConnectionInfo;

    public ClusterConnection(@Nullable String clusterUrl) {
        clusterConnectionInfo = StringUtils.isEmpty(clusterUrl) ? startElasticsearchContainer() : parseUrl(clusterUrl);

        if (clusterConnectionInfo != null) {
            logger.debug(clusterConnectionInfo.toString());
            clusterConnectionInfoThreadLocal.set(clusterConnectionInfo);
        } else {
            logger.error("could not create ClusterConnection");
        }
    }

    @Nullable
    public static ClusterConnectionInfo clusterConnectionInfo() {
        return clusterConnectionInfoThreadLocal.get();
    }

    @Nullable
    public ClusterConnectionInfo getClusterConnectionInfo() {
        return clusterConnectionInfo;
    }

    private ClusterConnectionInfo parseUrl(String clusterUrl) {
        try {
            URL url = new URL(clusterUrl);

            if (!url.getProtocol().startsWith("http") || url.getPort() <= 0) {
                throw new ClusterConnectionException("invalid Url: " + clusterUrl);
            }

            return ClusterConnectionInfo.builder()
                    .withHostAndPort(url.getHost(), url.getPort())
                    .useSsl(url.getProtocol().equals("https"))
                    .build();
        } catch (MalformedURLException e) {
            throw new ClusterConnectionException(e);
        }
    }

    @Nullable
    private ClusterConnectionInfo startElasticsearchContainer() {
        logger.debug("Start Elasticsearch Container");

        try {
            String elasticsearchVersion = VersionInfo.versionProperties().getProperty(VersionInfo.VERSION_ELASTICSEARCH_CLIENT);
            String dockerImageName = ELASTICSEARCH_DEFAULT_IMAGE + ':' + elasticsearchVersion;

            logger.info("Docker image: {}", dockerImageName);
            ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(dockerImageName);
            elasticsearchContainer.start();
            return ClusterConnectionInfo.builder()
                    .withHostAndPort(elasticsearchContainer.getHost(),
                            elasticsearchContainer.getMappedPort(ELASTICSEARCH_DEFAULT_PORT))
                    .withTransportPort(elasticsearchContainer.getMappedPort(ELASTICSEARCH_DEFAULT_TRANSPORT_PORT))
                    .withElasticsearchContainer(elasticsearchContainer)
                    .build();
        } catch (Exception e) {
            logger.error("could not start elasticsearch container");
        }

        return null;
    }

    @Override
    public void close() throws Throwable {
        if (clusterConnectionInfo != null && clusterConnectionInfo.getElasticsearchContainer() != null) {
            logger.debug("Stopping container");
            clusterConnectionInfo.getElasticsearchContainer().stop();
        }
        logger.debug("closed");
    }
}
