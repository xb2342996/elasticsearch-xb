package com.xxbb.springframework.data.elasticsearch.support;

import org.elasticsearch.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class VersionInfo {
    private static final Logger logger = LoggerFactory.getLogger(VersionInfo.class);
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final String VERSION_PROPERTIES = "versions.properties";

    public static final String VERSION_SPRING_DATA_ELASTICSEARCH = "version.spring-data-elasticsearch";
    public static final String VERSION_ELASTICSEARCH_CLIENT = "version.elasticsearch-client";

    public static void logVersions(@Nullable String clusterVersion) {
        if (!initialized.getAndSet(true)) {
            try {
                InputStream resource = VersionInfo.class.getClassLoader().getResourceAsStream(VERSION_PROPERTIES);
                if (resource != null) {
                    Properties properties = new Properties();
                    properties.load(resource);

                    String versionSpringDataElasticsearch = properties.getProperty(VERSION_SPRING_DATA_ELASTICSEARCH);
                    Version versionESBuilt = Version.fromString(properties.getProperty(VERSION_ELASTICSEARCH_CLIENT));
                    Version versionESUsed = Version.CURRENT;
                    Version versionESCluster = clusterVersion != null ? Version.fromString(clusterVersion) : null;

                    logger.info("Version Spring Data Elasticsearch: {}", versionSpringDataElasticsearch);
                    logger.info("Version Elasticsearch Client in build: {}", versionESBuilt.toString());
                    logger.info("Version Elasticsearch Client used: {}", versionESUsed.toString());

                    if (differInMajorOrMinor(versionESBuilt, versionESUsed)) {
                        logger.warn("Version mismatch in between Elasticsearch Clients build/use:{} - {}", versionESBuilt, versionESUsed);
                    }

                    if (versionESCluster != null) {
                        logger.info("Version Elasticsearch cluster: {}", versionESCluster.toString());
                        if (differInMajorOrMinor(versionESCluster, versionESUsed)) {
                            logger.warn("Version mismatch in between Elasticsearch client and cluster: {} - {}", versionESUsed, versionESCluster);
                        }
                    }
                } else {
                    logger.warn("cannot load {}", VERSION_PROPERTIES);
                }
            } catch (IOException e) {
                logger.warn("Could not log version info: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    public static Properties versionProperties() throws Exception {
        try {
            InputStream resource = VersionInfo.class.getClassLoader().getResourceAsStream(VERSION_PROPERTIES);
            if (resource != null) {
                Properties properties = new Properties();
                properties.load(resource);
                return properties;
            } else {
                throw new IllegalStateException("Resource not found");
            }
        } catch (Exception e) {
            logger.error("Could not load {}", VERSION_PROPERTIES, e);
            throw e;
        }
    }

    private static boolean differInMajorOrMinor(Version v1, Version v2) {
        return v1.major != v2.major || v1.minor != v2.minor;
    }

    private VersionInfo() {
    }
}
